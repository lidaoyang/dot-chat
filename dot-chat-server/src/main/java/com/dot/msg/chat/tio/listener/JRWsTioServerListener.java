package com.dot.msg.chat.tio.listener;

import com.alibaba.fastjson.JSON;
import com.dot.comm.constants.CommConstant;
import com.dot.comm.utils.RedisUtil;
import com.dot.msg.chat.em.ChatTypeEm;
import com.dot.msg.chat.model.ChatMsg;
import com.dot.msg.chat.service.ChatMsgService;
import com.dot.msg.chat.service.ChatUserService;
import com.dot.msg.chat.tio.em.CallTypeEm;
import com.dot.msg.chat.tio.em.MsgTypeEm;
import com.dot.msg.chat.tio.entiy.MessageCall;
import com.dot.msg.chat.tio.entiy.TioMessage;
import com.dot.msg.chat.tio.service.ChatMsgSendService;
import com.dot.msg.chat.tio.util.TioUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;
import org.tio.websocket.common.Opcode;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.common.WsSessionContext;
import org.tio.websocket.server.WsTioServerListener;

import java.nio.charset.StandardCharsets;

/**
 * 用户根据情况来完成该类的实现
 */
@Slf4j
@Component
public class JRWsTioServerListener extends WsTioServerListener {

    @Resource
    private ChatUserService chatUserService;

    @Resource
    private ChatMsgSendService chatMsgSendService;

    @Resource
    private ChatMsgService chatMsgService;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected,
                                 boolean isReconnect) throws Exception {
        super.onAfterConnected(channelContext, isConnected, isReconnect);
        if (log.isDebugEnabled()) {
            log.debug("onAfterConnected--{}--isConnected:{}--isReconnect:{}", channelContext, isConnected, isReconnect);
        }
    }

    @Override
    public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) throws Exception {
        super.onAfterSent(channelContext, packet, isSentSuccess);
        if (log.isDebugEnabled()) {
            log.debug("onAfterSent--{}--{}--{}", packet.logstr(), channelContext, isSentSuccess);
        }
    }

    @Override
    public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark,
                              boolean isRemove) throws Exception {
        super.onBeforeClose(channelContext, throwable, remark, isRemove);
        if (log.isDebugEnabled()) {
            log.debug("onBeforeClose--{}", channelContext);
        }

        WsSessionContext wsSessionContext = (WsSessionContext) channelContext.get();
        if (wsSessionContext != null && wsSessionContext.isHandshaked()) {
            int count = Tio.getAll(channelContext.getTioConfig()).getObj().size() - 1;
            String msg = "用户ID[" + channelContext.userid + "]离开了，现在共有【" + count + "】人在线";
            log.info(msg);
            // 用户下线
            userOffline(channelContext);
            String msgId = redisUtil.get(CommConstant.CHAT_MSG_CALLING_KEY + channelContext.userid);
            if (StringUtils.isNotBlank(msgId)) {
                log.warn("用户异常下线,通话中断,离线用户ID:{},msgId:{},isOnline:{}", channelContext.userid, msgId, TioUtil.isOnline(channelContext.tioConfig, channelContext.userid));
                // 发送通话中断信息
                sendDroppedMsg(channelContext, msgId);
            }

        }
    }

    private void sendDroppedMsg(ChannelContext channelContext, String msgIdStr) {
        ChatMsg chatMsg = chatMsgService.getById(msgIdStr);
        MessageCall oldMsgCall = JSON.parseObject(chatMsg.getMsg(), MessageCall.class);
        if (oldMsgCall.getCallType() == CallTypeEm.invite
                || oldMsgCall.getCallType() == CallTypeEm.hangup
                || oldMsgCall.getCallType() == CallTypeEm.refuse
                || oldMsgCall.getCallType() == CallTypeEm.cancel
                || oldMsgCall.getCallType() == CallTypeEm.no_answer
                || oldMsgCall.getCallType() == CallTypeEm.dropped) { // 异常中断时,通话未接通或已经结束不发送通话中断信息
            return;
        }
        int sendUserId = Integer.parseInt(channelContext.userid);
        MessageCall messageCall = new MessageCall();
        messageCall.setCallType(CallTypeEm.dropped);
        messageCall.setMsgId(chatMsg.getId());
        messageCall.setDroppedUserId(sendUserId);

        TioMessage message = new TioMessage(MsgTypeEm.getMstType(chatMsg.getMsgType()), JSON.toJSONString(messageCall));
        message.setId(messageCall.getMsgId());
        message.setChatType(ChatTypeEm.SINGLE);
        message.setSendUserId(sendUserId);
        message.setToUserId(chatMsg.getToUserId() == sendUserId ? chatMsg.getSendUserId() : chatMsg.getToUserId());

        chatMsgSendService.sendAndSaveMsg(channelContext, message);
    }

    private void userOffline(ChannelContext channelContext) {
        if (StringUtils.isBlank(channelContext.userid)) {
            return;
        }
        // 用户下线更新用户状态
        boolean updated = chatUserService.updateOnlineStatus(Integer.parseInt(channelContext.userid), false);
        if (!updated) {
            log.error("更新用户下线状态失败,chatUserId:{}", channelContext.userid);
        }
    }

    @Override
    public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) throws Exception {
        super.onAfterDecoded(channelContext, packet, packetSize);
        WsRequest wsRequest = (WsRequest) packet;
        Opcode opcode = wsRequest.getWsOpcode();
        if (opcode == Opcode.TEXT) {
            String text = wsRequest.getWsBodyText();
            if (log.isDebugEnabled()) {
                log.debug("onAfterDecoded--{}--{}", text, channelContext);
            }
            if (wsRequest.getBody() == null || wsRequest.getBody().length == 0) {
                log.warn("消息内容为空");
                String msg = "空";
                wsRequest.setBody(msg.getBytes(StandardCharsets.UTF_8));
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("onAfterDecoded--{}--{}", packet.logstr(), packetSize);
        }
    }

    @Override
    public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {
        super.onAfterReceivedBytes(channelContext, receivedBytes);
        if (log.isDebugEnabled()) {
            log.debug("onAfterReceivedBytes--{}", channelContext);
        }
    }

    @Override
    public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {
        super.onAfterHandled(channelContext, packet, cost);
        if (log.isDebugEnabled()) {
            log.debug("onAfterHandled--{}--{}", packet.logstr(), channelContext);
        }
    }

}
