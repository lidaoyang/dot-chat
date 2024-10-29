package com.dot.msg.chat.listener;

import cn.hutool.core.date.DateUtil;
import com.dot.comm.constants.TioConstant;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.exception.ApiException;
import com.dot.msg.chat.em.ChatTypeEm;
import com.dot.msg.chat.listener.event.*;
import com.dot.msg.chat.model.ChatMsg;
import com.dot.msg.chat.model.ChatMsgUserRel;
import com.dot.msg.chat.model.ChatRoom;
import com.dot.msg.chat.service.ChatMsgService;
import com.dot.msg.chat.service.ChatMsgUserRelService;
import com.dot.msg.chat.service.ChatRoomService;
import com.dot.msg.chat.tio.config.JRTioConfig;
import com.dot.msg.chat.tio.em.EventTypeEm;
import com.dot.msg.chat.tio.em.MsgTypeEm;
import com.dot.msg.chat.tio.entiy.TioMessage;
import com.dot.msg.chat.tio.service.ChatMsgSendService;
import com.dot.msg.chat.tio.util.TioUtil;
import com.dot.msg.notify.em.NotifyTypeEm;
import com.dot.msg.notify.request.NotifyMsgSendRequest;
import com.dot.msg.notify.service.NotifyMsgService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tio.core.Tio;
import org.tio.websocket.common.WsResponse;

import javax.annotation.Resource;

/**
 * 群聊事件监听
 *
 * @author: Dao-yang.
 * @date: Created in 2024/3/19 16:57
 */
@Slf4j
@Component
public class ChatGroupListener {

    @Resource
    private ChatRoomService chatRoomService;

    @Resource
    private ChatMsgService chatMsgService;

    @Resource
    private NotifyMsgService notifyMsgService;

    @Resource
    private ChatMsgSendService chatMsgSendService;

    @Resource
    private ChatMsgUserRelService chatMsgUserRelService;

    @Resource
    private JRTioConfig jrTioConfig;

    @TransactionalEventListener(ChatGroupCreateEvent.class)
    public void onGroupCreate(ChatGroupCreateEvent event) {
        log.info("群聊创建事件：{}", event);
        // 获取和保存聊天室
        ChatRoom chatRoom = getAndSaveChatRoom(event);
        // 保存聊天室关联关系数据
        saveChatRoomRel(event, chatRoom);
        // 绑定群组
        Tio.bindGroup(jrTioConfig.getTioConfig(), event.getUserId().toString(), event.getGroupId().toString());
        // 保存和发送聊天信息
        saveAndSendMsg(event, chatRoom);
        // 绑定群组成员
        event.getGroupMemberIds().forEach(memberId -> Tio.bindGroup(jrTioConfig.getTioConfig(), memberId.toString(), event.getGroupId().toString()));
    }

    private ChatRoom getAndSaveChatRoom(ChatGroupCreateEvent event) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatId(TioUtil.generateChatId(event.getGroupId()));
        chatRoom.setChatType(ChatTypeEm.GROUP.name());
        chatRoom.setGroupId(event.getGroupId());
        boolean saved = chatRoomService.save(chatRoom);
        if (!saved) {
            log.error("添加群聊聊天室失败,chatRoom:{}", chatRoom);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "添加群聊聊天室失败");
        }
        return chatRoom;
    }

    private void saveChatRoomRel(ChatGroupCreateEvent event, ChatRoom chatRoom) {
        boolean added = chatRoomService.addChatRoomUserRel(chatRoom.getChatId(), event.getUserId());
        if (!added) {
            log.error("添加群聊聊天室用户关系失败：{}", chatRoom);
        }
    }

    private void saveAndSendMsg(ChatGroupCreateEvent event, ChatRoom chatRoom) {
        ChatMsg chatMsg = getAndSaveChatMsg(event, chatRoom);
        saveChatMsgRel(event, chatMsg);
        sendGroupMsg(event, chatMsg);
        chatRoomService.updateLastMsg(chatMsg);
    }

    private ChatMsg getAndSaveChatMsg(ChatGroupCreateEvent event, ChatRoom chatRoom) {
        String msgContent = "你邀请" + event.getNicknameStr() + "加入了群聊";
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setChatId(chatRoom.getChatId());
        chatMsg.setChatType(chatRoom.getChatType());
        chatMsg.setGroupId(chatRoom.getGroupId());
        chatMsg.setSendUserId(event.getUserId());
        chatMsg.setToUserId(chatRoom.getGroupId());
        chatMsg.setMsgType(MsgTypeEm.SYSTEM.name());
        chatMsg.setMsg(msgContent);
        chatMsg.setSendTime(DateUtil.now());
        chatMsg.setTimestamp(DateUtil.current());
        boolean saved1 = chatMsgService.save(chatMsg);
        if (!saved1) {
            log.error("保存聊天信息失败,chatMsg:{}", chatMsg);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "保存聊天信息失败");
        }
        return chatMsg;
    }

    private void saveChatMsgRel(ChatGroupCreateEvent event, ChatMsg chatMsg) {
        ChatMsgUserRel chatMsgUserRel = new ChatMsgUserRel();
        chatMsgUserRel.setMsgId(chatMsg.getId());
        chatMsgUserRel.setUserId(event.getUserId());
        chatMsgUserRel.setChatId(chatMsg.getChatId());
        chatMsgUserRel.setIsUnread(false);
        chatMsgUserRel.setIsOffline(false);
        boolean saved2 = chatMsgUserRelService.save(chatMsgUserRel);
        if (!saved2) {
            log.error("保存聊天信息关联数据失败,chatMsgUserRel:{}", chatMsgUserRel);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "保存聊天信息关联数据失败");
        }
    }

    private void sendGroupMsg(ChatGroupCreateEvent event, ChatMsg chatMsg) {
        TioMessage message = getGroupCreateTioMessage(event, chatMsg);
        WsResponse meResponse = WsResponse.fromText(message.toString(), TioConstant.CHARSET);
        Tio.sendToGroup(jrTioConfig.getTioConfig(), event.getGroupId().toString(), meResponse);
    }

    private TioMessage getGroupCreateTioMessage(ChatGroupCreateEvent event, ChatMsg chatMsg) {
        TioMessage message = new TioMessage(MsgTypeEm.SYSTEM, chatMsg.getMsg());
        message.setChatId(chatMsg.getChatId());
        message.setSendUserId(event.getUserId());
        message.setChatType(ChatTypeEm.GROUP);
        message.setMsgType(MsgTypeEm.SYSTEM);
        message.setEventType(EventTypeEm.GROUP_CREATE);
        message.setToUserId(event.getGroupId());
        message.setSendTime(DateUtil.now());
        return message;
    }

    @TransactionalEventListener(ChatGroupNameUpdateEvent.class)
    public void onGroupNameUpdate(ChatGroupNameUpdateEvent event) {
        log.info("群名称更新事件：{}", event);
        String msg = event.getNickname() + "修改群名为“" + event.getGroupName() + "”";
        TioMessage tioMessage = getGroupInfoUpdateTioMessage(msg, event.getGroupId(), event.getUserId());
        chatMsgSendService.sendAndSaveMsg(TioUtil.getChannelContext(jrTioConfig.getTioConfig(), event.getUserId().toString()), tioMessage);
    }

    @TransactionalEventListener(ChatGroupNoticeUpdateEvent.class)
    public void onGroupNoticeUpdate(ChatGroupNoticeUpdateEvent event) {
        log.info("群名称更新事件：{}", event);
        String msg = event.getNickname() + "更新了群公告";
        TioMessage tioMessage = getGroupInfoUpdateTioMessage(msg, event.getGroupId(), event.getUserId());
        chatMsgSendService.sendAndSaveMsg(TioUtil.getChannelContext(jrTioConfig.getTioConfig(), event.getUserId().toString()), tioMessage);
    }

    private TioMessage getGroupInfoUpdateTioMessage(String msg, Integer groupId, Integer userId) {
        TioMessage message = new TioMessage(MsgTypeEm.SYSTEM, msg);
        message.setChatId(TioUtil.generateChatId(groupId));
        message.setSendUserId(userId);
        message.setChatType(ChatTypeEm.GROUP);
        message.setMsgType(MsgTypeEm.SYSTEM);
        message.setToUserId(groupId);
        message.setSendTime(DateUtil.now());
        return message;
    }

    @TransactionalEventListener(ChatGroupMemberRemoveEvent.class)
    public void onGroupMemberRemove(ChatGroupMemberRemoveEvent event) {
        log.info("管理员移除群成员事件：{}", event);
        String msg = "“" + event.getNicknameStr() + "”被管理员移出了群聊";
        event.getUserIds().forEach(userId -> {
            TioMessage tioMessage = getGroupLogoutTioMessage(msg, event.getGroupId(), userId, EventTypeEm.GROUP_KICK);
            chatMsgSendService.sendAndSaveMsg(TioUtil.getChannelContext(jrTioConfig.getTioConfig(), userId.toString()), tioMessage);
            // 解绑群聊
            Tio.unbindGroup(jrTioConfig.getTioConfig(), userId.toString(), event.getGroupId().toString());
        });
    }

    @TransactionalEventListener(ChatGroupLogoutEvent.class)
    public void onGroupLogout(ChatGroupLogoutEvent event) {
        log.info("退出群聊事件：{}", event);
        String chatId = TioUtil.generateChatId(event.getGroupId());
        // 删除群聊用户关系
        chatRoomService.deleteChatRoomUserRel(chatId, event.getUserId());
        // 清空聊天记录
        chatMsgService.cleanMsgList(event.getUserId(), chatId);
        // 退出群聊绑定
        Tio.unbindGroup(jrTioConfig.getTioConfig(), event.getUserId().toString(), event.getGroupId().toString());

        String msg = "“" + event.getNickname() + "”退出了群聊";
        TioMessage tioMessage = getGroupLogoutTioMessage(msg, event.getGroupId(), event.getUserId(), EventTypeEm.GROUP_EXIT);
        chatMsgSendService.sendAndSaveMsg(TioUtil.getChannelContext(jrTioConfig.getTioConfig(), event.getUserId().toString()), tioMessage);
    }

    private TioMessage getGroupLogoutTioMessage(String msg, Integer groupId, Integer userId, EventTypeEm eventType) {
        TioMessage message = new TioMessage(MsgTypeEm.SYSTEM, msg);
        message.setChatId(TioUtil.generateChatId(groupId));
        message.setSendUserId(userId);
        message.setChatType(ChatTypeEm.GROUP);
        message.setMsgType(MsgTypeEm.EVENT);
        message.setEventType(eventType);
        message.setToUserId(groupId);
        message.setSendTime(DateUtil.now());
        return message;
    }

    @TransactionalEventListener(ChatGroupMemberAddEvent.class)
    public void onGroupMemberAdd(ChatGroupMemberAddEvent event) {
        log.info("管理员添加群成员事件：{}", event);
        event.getGroupMemberIds().forEach(memberId -> {
            Tio.bindGroup(jrTioConfig.getTioConfig(), memberId.toString(), event.getGroupId().toString());
        });
        String msg = "“" + event.getNicknameStr() + "”加入了群聊";
        if (StringUtils.isNotBlank(event.getNickname())) {
            msg = "“" + event.getNickname() + "”邀请" + msg;
        }
        TioMessage tioMessage = getGroupMemberAddTioMessage(msg, event.getGroupId(), event.getUserId());
        chatMsgSendService.sendAndSaveMsg(TioUtil.getChannelContext(jrTioConfig.getTioConfig(), event.getUserId().toString()), tioMessage);
    }

    private TioMessage getGroupMemberAddTioMessage(String msg, Integer groupId, Integer userId) {
        TioMessage message = new TioMessage(MsgTypeEm.SYSTEM, msg);
        message.setChatId(TioUtil.generateChatId(groupId));
        message.setSendUserId(userId);
        message.setChatType(ChatTypeEm.GROUP);
        message.setMsgType(MsgTypeEm.EVENT);
        message.setEventType(EventTypeEm.GROUP_JOIN);
        message.setToUserId(groupId);
        message.setSendTime(DateUtil.now());
        return message;
    }

    @TransactionalEventListener(ChatGroupApplyEvent.class)
    public void onGroupApply(ChatGroupApplyEvent event) {
        log.info("入群申请事件：{}", event);
        event.getManagerList().forEach(managerId -> {
            NotifyMsgSendRequest request = getNotifyMsgSendRequest(event, managerId);
            notifyMsgService.sendNotifyMsg(request);
        });
    }

    private NotifyMsgSendRequest getNotifyMsgSendRequest(ChatGroupApplyEvent event, Integer managerId) {
        NotifyMsgSendRequest request = new NotifyMsgSendRequest();
        request.setLinkid(event.getApplyId().toString());
        request.setSendUserId(event.getUserId());
        request.setToUserId(managerId);
        request.setNotifyType(NotifyTypeEm.USER);
        request.setMsgType(MsgTypeEm.EVENT);
        request.setEventType(EventTypeEm.GROUP_APPLY);
        request.setMsgContent("你收到一个入群申请消息,请尽快处理");
        request.setSendTime(DateUtil.now());
        return request;
    }

}
