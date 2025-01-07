package com.dot.msg.chat.tio.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.dot.comm.constants.CommConstant;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.entity.LoginUsername;
import com.dot.comm.exception.ApiException;
import com.dot.comm.manager.TokenManager;
import com.dot.comm.utils.RedisUtil;
import com.dot.msg.chat.dto.ChatUserMsgDto;
import com.dot.msg.chat.em.ChatTypeEm;
import com.dot.msg.chat.listener.event.UserRegisterAddDefaultFriendEvent;
import com.dot.msg.chat.model.ChatGroupMember;
import com.dot.msg.chat.model.ChatUser;
import com.dot.msg.chat.service.ChatGroupMemberService;
import com.dot.msg.chat.service.ChatMsgService;
import com.dot.msg.chat.service.ChatRoomService;
import com.dot.msg.chat.service.ChatUserService;
import com.dot.msg.chat.tio.em.EventTypeEm;
import com.dot.msg.chat.tio.em.MsgTypeEm;
import com.dot.msg.chat.tio.entiy.TioMessage;
import com.dot.msg.chat.tio.service.ChatMsgSendService;
import com.dot.msg.notify.dto.NotifyMsgInfoDto;
import com.dot.msg.notify.em.NotifyBizEm;
import com.dot.msg.notify.service.NotifyMsgService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.server.handler.IWsMsgHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tanyaowu
 * 2017年6月28日 下午5:32:38
 */
@Slf4j
@Component
public class JRWsMsgHandler implements IWsMsgHandler {

    @Resource
    private TokenManager tokenManager;

    @Resource
    private ChatUserService chatUserService;

    @Resource
    private ChatGroupMemberService chatGroupMemberService;

    @Resource
    private ChatRoomService chatRoomService;

    @Resource
    private ChatMsgService chatMsgService;

    @Resource
    private ChatMsgSendService chatMsgSendService;

    @Resource
    private NotifyMsgService notifyMsgService;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource(name = "redisUtil")
    private RedisUtil redisUtil;


    /**
     * 握手时走这个方法，业务可以在这里获取cookie，request参数等
     */
    @Override
    public HttpResponse handshake(HttpRequest request, HttpResponse httpResponse, ChannelContext channelContext) {
        String clientip = request.getClientIp();
        log.info("收到来自{}的ws握手包--{}", clientip, request);
        return httpResponse;
    }

    /**
     * 握手成功后触发该方法
     */
    @Override
    public void onAfterHandshaked(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) {
        String token = httpRequest.getParam("token");
        // 获取登录用户
        LoginUsername loginUser;
        try {
            loginUser = tokenManager.getLoginUser(token);
        } catch (Exception e) {
            log.error("token错误", e);
            sendUnAuthSystemMsg(channelContext, channelContext.userid);
            return;
        }
        // 获取聊天用户
        ChatUser chatUser = getChatUser(loginUser);

        // 绑定到用户
        Tio.bindUser(channelContext, chatUser.getId().toString());

        // 绑定到群组
        bindGroup(channelContext, chatUser);

        // 绑定到业务ID
        // bindBizId(channelContext, chatUser);

        // 发送离线消息
        sendOfflineMsg(channelContext, chatUser);

        // 发送离线通知消息
        sendOfflineNotifyMsg(channelContext, chatUser);

        // 注册用户添加默认好友
        registerUserAddDefaultFriendEvent(chatUser);
    }

    private void registerUserAddDefaultFriendEvent(ChatUser chatUser) {
        if (redisUtil.exists(CommConstant.CHAT_USER_FIRST_KEY+ chatUser.getId())){
            try {
                eventPublisher.publishEvent(new UserRegisterAddDefaultFriendEvent(chatUser.getId()));
            } catch (Exception e) {
                log.warn("注册用户添加默认好友失败,chatUser:{}", chatUser, e);
            }
        }
    }

    /**
     * 获取聊天用户
     *
     * @param loginUser 登录用户
     * @return 聊天用户
     */
    private ChatUser getChatUser(LoginUsername loginUser) {
        ChatUser chatUser = chatUserService.getChatUser(loginUser.getUid());
        // 如果用户不在线，则更新在线状态
        if (!chatUser.getIsOnline()) {
            chatUserService.updateOnlineStatus(chatUser.getId(), true);
        }
        log.info("用户{}已上线,ID:{}", chatUser.getNickname(), chatUser.getId());
        return chatUser;
    }

    /**
     * 绑定到群组
     *
     * @param channelContext channelContext
     * @param chatUser       聊天用户
     */
    private void bindGroup(ChannelContext channelContext, ChatUser chatUser) {
        List<ChatGroupMember> chatGroupMemberList = chatGroupMemberService.getChatGroupMemberListByUserId(chatUser.getId());
        chatGroupMemberList.forEach(chatGroupMember -> {
            // 绑定到群组
            Tio.bindGroup(channelContext, chatGroupMember.getGroupId().toString());
        });
    }

    /**
     * 绑定业务ID
     *
     * @param channelContext channelContext
     * @param chatUser       聊天用户
     */
    private void bindBizId(ChannelContext channelContext, ChatUser chatUser) {
            String bizid =  NotifyBizEm.BIZ_ORDER.getBizId(chatUser.getId());
            Tio.bindBsId(channelContext, bizid);
    }


    /**
     * 发送离线消息
     *
     * @param channelContext channelContext
     * @param chatUser       chatUser
     */
    private void sendOfflineMsg(ChannelContext channelContext, ChatUser chatUser) {
        List<ChatUserMsgDto> offlineMsgList = chatMsgService.getOfflineMsgList(chatUser.getId());
        if (CollUtil.isNotEmpty(offlineMsgList)) {
            log.info("发送离线消息,用户id:{},size:{}", chatUser.getId(), offlineMsgList.size());
            /* //发送离线消息,会出现重复问题
            offlineMsgList.forEach(offlineMsg -> {
                TioMessage message = getMessage(offlineMsg);
                if (ChatTypeEm.SINGLE.name().equals(offlineMsg.getChatType())) { // 单聊消息
                    // 把离线消息重新发送给登录的用户
                    chatMsgSendService.sendToUser(channelContext.tioConfig, message);
                } else if (ChatTypeEm.GROUP.name().equals(offlineMsg.getChatType())) { // 群聊消息
                    // 把离线消息重新发送给群组
                    chatMsgSendService.sendToGroup(channelContext.tioConfig, message);
                }
            });*/
            // 更新离线标志
            List<Integer> msgIds = offlineMsgList.stream().map(ChatUserMsgDto::getId).collect(Collectors.toList());
            boolean ret = chatMsgService.batchUpdateOfflineMsg(chatUser.getId(), msgIds);
            if (!ret) {
                log.error("批量更新离线消息失败,msgIds:{}", msgIds);
            }
            List<String> chatIds = offlineMsgList.stream().map(ChatUserMsgDto::getChatId).distinct().toList();
            boolean cleared = chatRoomService.clearOfflineMsgCount(chatIds, chatUser.getId());
            if (!cleared) {
                log.error("批量清除离线消息失败,chatIds:{},userId:{}", chatIds, chatUser.getId());
            }
        }
    }

    private TioMessage getMessage(ChatUserMsgDto offlineMsg) {
        TioMessage message = new TioMessage(MsgTypeEm.getMstType(offlineMsg.getMsgType()), offlineMsg.getMsg());
        message.setChatId(offlineMsg.getChatId());
        message.setSendUserId(offlineMsg.getSendUserId());
        message.setSendTime(offlineMsg.getSendTime());
        message.setChatType(ChatTypeEm.getMstType(offlineMsg.getChatType()));
        message.setToUserId(offlineMsg.getToUserId());
        return message;
    }

    /**
     * 发送离线通知消息
     *
     * @param channelContext 通道
     * @param chatUser       聊天用户
     */
    private void sendOfflineNotifyMsg(ChannelContext channelContext, ChatUser chatUser) {
        List<NotifyMsgInfoDto> offlineNotifyMsgList = notifyMsgService.getOfflineNotifyMsgList(chatUser.getId());
        if (CollUtil.isNotEmpty(offlineNotifyMsgList)) {
            log.info("发送离线通知消息,用户id:{},size:{}", chatUser.getId(), offlineNotifyMsgList.size());
            offlineNotifyMsgList.forEach(offlineNotifyMsg -> {
                TioMessage message = getTioMessage(offlineNotifyMsg);
                chatMsgSendService.sendToUser(channelContext.tioConfig, message);
            });
            // 更新离线通知消息状态
            notifyMsgService.updateOfflineNotifyMsg(chatUser.getId());
        }
    }

    private TioMessage getTioMessage(NotifyMsgInfoDto offlineNotifyMsg) {
        TioMessage message = new TioMessage();
        message.setMsgType(MsgTypeEm.getMstType(offlineNotifyMsg.getMsgType()));
        message.setEventType(EventTypeEm.getEventType(offlineNotifyMsg.getEventType()));
        message.setToUserId(offlineNotifyMsg.getToUserId());
        message.setMsg(offlineNotifyMsg.getMsgContent());
        message.setSendUserId(offlineNotifyMsg.getSendUserId());
        message.setSendTime(offlineNotifyMsg.getSendTime());
        return message;
    }

    private void sendUnAuthSystemMsg(ChannelContext channelContext, String sendUserId) {
        if (StringUtils.isBlank(sendUserId)) {
            log.error("系统内存中当前用户不存在");
            return;
        }
        TioMessage message = new TioMessage();
        message.setMsgType(MsgTypeEm.SYSTEM);
        message.setToUserId(Integer.valueOf(sendUserId));
        message.setMsg("权限不足");
        message.setCode(ExceptionCodeEm.UNAUTHORIZED.getCode());
        chatMsgSendService.sendToUser(channelContext.tioConfig, message);
    }

    private void check(String token) {
        if (StringUtils.isBlank(token)) {
            log.error("token为空");
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "token为空");
        }
    }

    /**
     * 字节消息（binaryType = arraybuffer）过来后会走这个方法
     */
    @Override
    public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) {
        return null;
    }

    /**
     * 当客户端发close flag时，会走这个方法
     */
    @Override
    public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) {
        Tio.remove(channelContext, "receive close flag");
        return null;
    }

    /*
     * 字符消息（binaryType = blob）过来后会走这个方法
     */
    @Override
    public Object onText(WsRequest wsRequest, String text, ChannelContext channelContext) {
        TioMessage message = JSON.parseObject(text, TioMessage.class);
        if (MsgTypeEm.HEART_BEAT == message.getMsgType()) {
            if (log.isDebugEnabled()) {
                log.debug("心跳检测");
            }
            return null;
        }
        message.setSendTime(DateUtil.now());
        if (StringUtils.isBlank(channelContext.userid)) {
            log.error("系统内存中当前用户不存在");
            return null;
        }
        // 消息内容为空时,发送系统警告
        if (StringUtils.isBlank(message.getMsg()) || "空".equals(message.getMsg())) {
            // 发送系统警告消息
            sendSystemWarnMsg(channelContext, message);
            log.info("消息内容为空,发送系统警告,text:{}", text);
            return null;
        }
        // 发送消息并保存聊天记录
        chatMsgSendService.sendAndSaveMsg(channelContext, message);

        // 返回值是要发送给客户端的内容，一般都是返回null
        return null;
    }

    private void sendSystemWarnMsg(ChannelContext channelContext, TioMessage message) {
        if (ChatTypeEm.SINGLE == message.getChatType()) {
            message.setMsgType(MsgTypeEm.SYSTEM_WARNING);
            message.setToUserId(Integer.parseInt(channelContext.userid));
            message.setMsg("消息内容不能为空");
            chatMsgSendService.sendToUser(channelContext.tioConfig, message);
        } else if (ChatTypeEm.GROUP == message.getChatType()) {
            message.setMsgType(MsgTypeEm.SYSTEM_WARNING);
            message.setMsg("消息内容不能为空");
            chatMsgSendService.sendToGroup(channelContext.tioConfig, message);
        }
    }
}
