package com.dot.msg.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.em.PageFlippingTypeEm;
import com.dot.comm.exception.ApiException;
import com.dot.msg.chat.dao.ChatMsgDao;
import com.dot.msg.chat.dto.ChatGroupMemberSimDto;
import com.dot.msg.chat.dto.ChatUserMsgDto;
import com.dot.msg.chat.dto.ChatUserSimDto;
import com.dot.msg.chat.em.ChatTypeEm;
import com.dot.msg.chat.listener.event.ChatMsgSendEvent;
import com.dot.msg.chat.model.ChatMsg;
import com.dot.msg.chat.model.ChatMsgUserRel;
import com.dot.msg.chat.model.ChatRoom;
import com.dot.msg.chat.request.ChatMsgAddRequest;
import com.dot.msg.chat.request.ChatMsgSearchRequest;
import com.dot.msg.chat.response.ChatUserMsgResponse;
import com.dot.msg.chat.response.ChatUserResponse;
import com.dot.msg.chat.service.*;
import com.dot.msg.chat.tio.config.JRTioConfig;
import com.dot.msg.chat.tio.em.CallTypeEm;
import com.dot.msg.chat.tio.em.EventTypeEm;
import com.dot.msg.chat.tio.em.MsgTypeEm;
import com.dot.msg.chat.tio.entiy.MessageCall;
import com.dot.msg.chat.tio.entiy.TioMessage;
import com.dot.msg.chat.tio.util.TioUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.tio.core.ChannelContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 会话消息表服务接口实现
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Slf4j
@Service
public class ChatMsgServiceImpl extends ServiceImpl<ChatMsgDao, ChatMsg> implements ChatMsgService {

    @Resource
    private ChatMsgUserRelService chatMsgUserRelService;

    @Resource
    private ChatUserService chatUserService;

    @Resource
    private ChatGroupMemberService chatGroupMemberService;

    @Resource
    private ChatRoomService chatRoomService;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private JRTioConfig jrTioConfig;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public ChatMsg get(Integer msgId) {
        LambdaQueryWrapper<ChatMsg> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatMsg::getId, ChatMsg::getChatId, ChatMsg::getChatType, ChatMsg::getGroupId, ChatMsg::getSendUserId, ChatMsg::getToUserId, ChatMsg::getMsgType, ChatMsg::getSendTime, ChatMsg::getTimestamp);
        queryWrapper.eq(ChatMsg::getId, msgId);
        return getOne(queryWrapper);
    }

    @Override
    public List<ChatUserMsgDto> getOfflineMsgList(Integer userId) {
        QueryWrapper<ChatUserMsgDto> queryWrapper = Wrappers.query();
        queryWrapper.eq("cmu.user_id", userId);
        queryWrapper.eq("cmu.is_offline", true);
        queryWrapper.orderByAsc("cmu.id");
        return baseMapper.selectUserMsgList(queryWrapper);
    }

    @Override
    public List<ChatUserMsgResponse> getUserMsgList(ChatMsgSearchRequest request) {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        if (request.getPageFlippingType() != PageFlippingTypeEm.FIRST
                && request.getPageFlippingType() != PageFlippingTypeEm.HIS_PULL_UP
                && ObjectUtil.isNull(request.getMsgId())) {
            log.error("翻页类型{},msgId不能为空", request.getPageFlippingType());
            throw new ApiException(ExceptionCodeEm.PRAM_NOT_MATCH, "msgId不能为空");
        }
        QueryWrapper<ChatUserMsgDto> queryWrapper = Wrappers.query();
        queryWrapper.eq("cm.chat_id", request.getChatId());
        queryWrapper.eq("cmu.user_id", chatUserId);
        if (ObjectUtil.isNotNull(request.getMsgType())) {
            if (request.getMsgType() == MsgTypeEm.ALL) {
                queryWrapper.in("cm.msg_type", MsgTypeEm.TEXT.name(), MsgTypeEm.PRODUCT.name(), MsgTypeEm.IMAGE.name(), MsgTypeEm.VIDEO.name(), MsgTypeEm.FILE.name(), MsgTypeEm.BIZ_CARD.name(), MsgTypeEm.GROUP_BIZ_CARD.name());
            } else {
                queryWrapper.eq("cm.msg_type", request.getMsgType().name());
            }
        }
        queryWrapper.eq(ObjectUtil.isNotNull(request.getSendUserId()), "cm.send_user_id", request.getSendUserId());
        queryWrapper.between(StringUtils.isNotBlank(request.getSendDate()), "cm.send_time", request.getSendDateStart(), request.getSendDateEnd());
        queryWrapper.like(StringUtils.isNotBlank(request.getKeywords()), "cm.msg", request.getKeywords());

        setOrderBySql(request, queryWrapper);
        queryWrapper.last(" limit " + request.getLimit());
        List<ChatUserMsgDto> chatUserMsgDtos = baseMapper.selectUserMsgList(queryWrapper);
        if (CollUtil.isEmpty(chatUserMsgDtos)) {
            return new ArrayList<>();
        }
        List<ChatUserMsgResponse> responseList = BeanUtil.copyToList(chatUserMsgDtos, ChatUserMsgResponse.class);
        // 获取聊天用户列表
        List<ChatUserSimDto> chatUserList = getChatUserList(chatUserMsgDtos, chatUserId);
        ChatUserMsgDto chatUserMsgDto = chatUserMsgDtos.get(0);
        if (chatUserMsgDto.getChatType().equals(ChatTypeEm.GROUP.name())) {
            List<ChatGroupMemberSimDto> groupMemberList = chatGroupMemberService.getChatGroupMemberList(chatUserId, chatUserMsgDto.getToUserId(), null);
            setGroupNicknameAvatar(responseList, groupMemberList, chatUserList);
        } else {
            setNicknameAvatar(responseList, chatUserList);
        }

        if (request.getPageFlippingType() == PageFlippingTypeEm.FIRST) { // 首次加载倒序
            // 倒序
            Collections.reverse(responseList);
            // 设置当前用户选中的聊天室id
            TioUtil.setCurrChatId(jrTioConfig.getTioConfig(), chatUserId.toString(), request.getChatId());
            // 清除未读消息
            chatRoomService.clearUnreadMsgCount(request.getChatId(), chatUserId);
        }
        return responseList;
    }

    private void setOrderBySql(ChatMsgSearchRequest request, QueryWrapper<ChatUserMsgDto> queryWrapper) {
        if (request.getPageFlippingType() == PageFlippingTypeEm.LOCATE) {
            queryWrapper.ge("cm.id", request.getMsgId());
            queryWrapper.orderByAsc("cmu.id");
        } else if (request.getPageFlippingType() == PageFlippingTypeEm.PULL_UP) {
            queryWrapper.gt("cm.id", request.getMsgId());
            queryWrapper.orderByAsc("cmu.id");
        } else if (request.getPageFlippingType() == PageFlippingTypeEm.PULL_DOWN || request.getPageFlippingType() == PageFlippingTypeEm.HIS_PULL_UP) {
            queryWrapper.lt(ObjectUtil.isNotNull(request.getMsgId()), "cm.id", request.getMsgId());
            queryWrapper.orderByDesc("cmu.id");
        } else {
            queryWrapper.orderByDesc("cmu.id");
        }
    }

    private void setGroupNicknameAvatar(List<ChatUserMsgResponse> responseList,
                                        List<ChatGroupMemberSimDto> groupMemberList,
                                        List<ChatUserSimDto> chatUserList) {
        responseList.forEach(chatMsg -> {
            groupMemberList.stream().filter(groupMember -> groupMember.getUserId().equals(chatMsg.getSendUserId())).findFirst().ifPresent(groupMember -> {
                chatMsg.setNickname(groupMember.getNickname());
                chatMsg.setAvatar(groupMember.getAvatar());
            });
            if (StringUtils.isBlank(chatMsg.getNickname())) {
                chatUserList.stream().filter(chatUser -> chatUser.getId().equals(chatMsg.getSendUserId())).findFirst().ifPresent(chatUser -> {
                    chatMsg.setNickname(chatUser.getNickname());
                    chatMsg.setAvatar(chatUser.getAvatar());
                });
            }
        });
    }

    private void setNicknameAvatar(List<ChatUserMsgResponse> responseList, List<ChatUserSimDto> chatUserList) {
        responseList.forEach(chatMsg -> {
            chatUserList.stream().filter(chatUser -> chatUser.getId().equals(chatMsg.getSendUserId())).findFirst().ifPresent(chatUser -> {
                chatMsg.setNickname(chatUser.getNickname());
                chatMsg.setAvatar(chatUser.getAvatar());
            });
        });
    }

    private List<ChatUserSimDto> getChatUserList(List<ChatUserMsgDto> chatUserMsgDtos, Integer currentUserId) {
        List<Integer> sendUserIds = chatUserMsgDtos.stream().map(ChatUserMsgDto::getSendUserId).distinct().toList();
        return chatUserService.getChatUserLeftFriendSimList(sendUserIds, currentUserId);
    }

    @Override
    public ChatUserMsgResponse getLastCallMsg() {
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        LambdaQueryWrapper<ChatMsg> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(ChatMsg::getMsgType, MsgTypeEm.AUDIO_CALL.name(), MsgTypeEm.VIDEO_CALL.name());
        queryWrapper.and(wrapper ->{
            wrapper.eq(ChatMsg::getSendUserId, chatUser.getId())
                    .or().eq(ChatMsg::getToUserId, chatUser.getId());
        });
        queryWrapper.orderByDesc(ChatMsg::getId);
        queryWrapper.last("limit 1");
        ChatMsg chatMsg = this.getOne(queryWrapper);
        if (ObjectUtil.isNull(chatMsg)) {
            return null;
        }
        MessageCall messageCall = JSON.parseObject(chatMsg.getMsg(), MessageCall.class);
        if (messageCall.getCallType() != CallTypeEm.invite) {
            return null;
        }
        return BeanUtil.copyProperties(chatMsg, ChatUserMsgResponse.class);
    }

    @Override
    public boolean updateOfflineMsg(Integer userId, Integer msgId) {
        LambdaUpdateWrapper<ChatMsgUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatMsgUserRel::getUserId, userId).eq(ChatMsgUserRel::getMsgId, msgId);
        updateWrapper.set(ChatMsgUserRel::getIsOffline, false);
        return chatMsgUserRelService.update(updateWrapper);
    }

    @Override
    public boolean batchUpdateOfflineMsg(Integer userId, List<Integer> msgIds) {
        if (CollUtil.isEmpty(msgIds)) {
            return false;
        }
        LambdaUpdateWrapper<ChatMsgUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatMsgUserRel::getUserId, userId);
        if (msgIds.size() == 1) {
            updateWrapper.eq(ChatMsgUserRel::getMsgId, msgIds.get(0));
        } else {
            updateWrapper.in(ChatMsgUserRel::getMsgId, msgIds);
        }
        updateWrapper.set(ChatMsgUserRel::getIsOffline, false);
        return chatMsgUserRelService.update(updateWrapper);
    }

    @Override
    public boolean updateMsgContent(Integer msgId, String msgContent) {
        if (ObjectUtil.isNotNull(msgId) && StringUtils.isNotBlank(msgContent)) {
            LambdaUpdateWrapper<ChatMsg> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(ChatMsg::getId, msgId);
            updateWrapper.set(ChatMsg::getMsg, msgContent);
            return update(updateWrapper);
        }
        return false;
    }

    @Override
    public Integer saveMsg(ChatMsgAddRequest request) {
        if (request == null) {
            return null;
        }
        log.info("保存一条聊天信息,chatId:{}", request.getChatId());
        // 保存消息记录
        ChatMsg chatMsg = getChatMsg(request);
        Boolean executed = transactionTemplate.execute(t -> {
            boolean saved = this.save(chatMsg);
            if (saved) {
                List<ChatMsgUserRel> chatMsgUserRelList = getNewChatMsgUserRelList(request, chatMsg);
                // 保存用户消息记录
                saveChatMsgRel(chatMsgUserRelList);
                // 更新聊天室最新消息
                updateLastMsg(chatMsg);
                // 更新未读消息数+1
                updateUnreadMsgCount(request, chatMsgUserRelList);
                // 更新离线消息数
                updateOfflineMsgCount(request, chatMsgUserRelList);
            }
            return saved;
        });
        if (Boolean.TRUE.equals(executed)) {
            return chatMsg.getId();
        } else {
            log.error("保存消息记录失败,request:{},chatMsg:{}", JSON.toJSONString(request), JSON.toJSONString(chatMsg));
        }
        return null;
    }

    private void saveChatMsgRel(List<ChatMsgUserRel> chatMsgUserRelList) {
        boolean ret = chatMsgUserRelService.saveBatch(chatMsgUserRelList);
        if (!ret) {
            log.error("保存关联用户信息失败,chatMsgUserRelList:{}", JSON.toJSONString(chatMsgUserRelList));
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "保存关联用户信息失败");
        }
    }

    private void updateLastMsg(ChatMsg chatMsg) {
        boolean updated = chatRoomService.updateLastMsg(chatMsg);
        if (!updated) {
            log.error("更新聊天室最新消息失败,chatMsg:{}", JSON.toJSONString(chatMsg));
        }
    }

    private void updateUnreadMsgCount(ChatMsgAddRequest request, List<ChatMsgUserRel> chatMsgUserRelList) {
        List<Integer> unreadUserIds = chatMsgUserRelList.stream().filter(ChatMsgUserRel::getIsUnread).map(ChatMsgUserRel::getUserId).toList();
        boolean updated1 = chatRoomService.updateUnreadMsgCount(request.getChatId(), unreadUserIds, 1);
        if (!updated1) {
            log.warn("未更新聊天室未读消息,chatId:{}", request.getChatId());
        }
    }

    private void updateOfflineMsgCount(ChatMsgAddRequest request, List<ChatMsgUserRel> chatMsgUserRelList) {
        List<Integer> offlineUserIds = chatMsgUserRelList.stream().filter(ChatMsgUserRel::getIsOffline).map(ChatMsgUserRel::getUserId).toList();
        boolean updated2 = chatRoomService.batchUpdateOfflineMsgCount(request.getChatId(), offlineUserIds, 1);
        if (!updated2) {
            log.info("未更新聊天室离线消息数,chatId:{}", request.getChatId());
        }
    }

    private ChatMsg getChatMsg(ChatMsgAddRequest request) {
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setChatId(request.getChatId());
        chatMsg.setChatType(request.getChatType());
        chatMsg.setGroupId(request.getGroupId());
        chatMsg.setSendUserId(request.getSendUserId());
        chatMsg.setToUserId(request.getToUserId());
        chatMsg.setMsgType(request.getMsgType());
        chatMsg.setMsg(request.getMsg());
        chatMsg.setSendTime(DateUtil.now());
        chatMsg.setTimestamp(DateUtil.current());
        return chatMsg;
    }

    private List<ChatMsgUserRel> getNewChatMsgUserRelList(ChatMsgAddRequest request, ChatMsg chatMsg) {
        List<ChatMsgUserRel> chatMsgUserRelList = new ArrayList<>();
        List<Integer> userIdList = new ArrayList<>();
        if (request.getChatType().equals(ChatTypeEm.SINGLE.name())) {
            userIdList.add(request.getSendUserId());
            if (!request.getSendUserId().equals(request.getToUserId())) {
                userIdList.add(request.getToUserId());
            }
        } else {
            List<Integer> groupMemberIdList = chatGroupMemberService.getChatGroupMemberIdListByGroupId(request.getGroupId());
            if (CollUtil.isNotEmpty(groupMemberIdList)) {
                userIdList.addAll(groupMemberIdList);
            }
        }
        if (CollUtil.isEmpty(userIdList)) {
            log.error("用户列表为空,request:{}", JSON.toJSONString(request));
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "用户列表为空");
        }

        userIdList.forEach(userId -> {
            ChatMsgUserRel chatMsgUserRel = getNewChatMsgUserRel(userId, request, chatMsg.getId());
            chatMsgUserRelList.add(chatMsgUserRel);
        });
        return chatMsgUserRelList;
    }

    private ChatMsgUserRel getNewChatMsgUserRel(Integer userId, ChatMsgAddRequest request, Integer msgId) {
        boolean isOffline = TioUtil.isOffline(jrTioConfig.getTioConfig(), userId.toString());
        ChatMsgUserRel chatMsgUserRel = new ChatMsgUserRel();
        chatMsgUserRel.setMsgId(msgId);
        chatMsgUserRel.setUserId(userId);
        chatMsgUserRel.setChatId(request.getChatId());
        chatMsgUserRel.setIsUnread(isUnread(userId, request.getChatId()));
        chatMsgUserRel.setIsOffline(isOffline);
        return chatMsgUserRel;
    }

    private boolean isUnread(Integer userId, String chatId) {
        return TioUtil.isUnread(jrTioConfig.getTioConfig(), userId, chatId);
    }

    @Override
    public boolean cleanMsgList(Integer userId, String chatId) {
        return chatMsgUserRelService.cleanMsgList(userId, chatId);
    }

    @Override
    public boolean relayMsg(List<Integer> msgIds, List<String> chatIds, List<Integer> toUserIds) {
        if (CollUtil.isEmpty(chatIds) && CollUtil.isEmpty(toUserIds)) {
            log.error("聊天室ID和转发用户ID不能同时为空");
            throw new ApiException(ExceptionCodeEm.PRAM_NOT_MATCH, "聊天室ID和转发用户ID不能同时为空");
        }
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        List<ChatMsg> chatMsgList = getChatMsgList(msgIds);
        if (chatMsgList.size() != msgIds.size()) {
            log.warn("聊天消息id存在错误,chatUserId:{},msgIds:{}", chatUser.getId(), msgIds);
        }
        ChannelContext channelContext = TioUtil.getChannelContext(jrTioConfig.getTioConfig(), chatUser.getId().toString());
        if (channelContext == null) {
            log.warn("当前用户不在线,chatUserId:{}", chatUser.getId());
            return false;
        }
        List<Integer> toGroupIds = new ArrayList<>();
        if (CollUtil.isNotEmpty(chatIds)) {
            toUserIds = new ArrayList<>();
            log.info("转发消息到最近聊天室,chatUserId:{},chatIds:{},msgIds:{}", chatUser.getId(), chatIds, msgIds);
            addToGroupIdsAndUserIds(chatIds, toUserIds, toGroupIds, chatUser);
        }
        List<TioMessage> messageList = getTioMessageList(chatMsgList, toUserIds, toGroupIds, chatUser);
        // 发送消息
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            // 发送消息
            eventPublisher.publishEvent(new ChatMsgSendEvent(channelContext, messageList, "转发"));
            return true;
        }));
    }

    private List<ChatMsg> getChatMsgList(List<Integer> msgIds) {
        LambdaQueryWrapper<ChatMsg> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(ChatMsg::getId, msgIds);
        return this.list(queryWrapper);
    }

    private void addToGroupIdsAndUserIds(List<String> chatIds, List<Integer> toUserIds, List<Integer> toGroupIds,
                                         ChatUserResponse chatUser) {
        List<ChatRoom> chatRoomList = chatRoomService.getList(chatIds);
        chatRoomList.forEach(chatRoom -> {
            if (chatRoom.getChatType().equals(ChatTypeEm.GROUP.name())) {
                toGroupIds.add(chatRoom.getGroupId());
            } else {
                List<Integer> chatUserIds = TioUtil.getChatUserIdsByChatId(chatRoom.getChatId());
                chatUserIds.remove(chatUser.getId());
                toUserIds.add(chatUserIds.get(0));
            }
        });
    }

    private List<TioMessage> getTioMessageList(List<ChatMsg> chatMsgList, List<Integer> toUserIds,
                                               List<Integer> toGroupIds, ChatUserResponse chatUser) {
        List<TioMessage> messageList = new ArrayList<>();
        chatMsgList.forEach(chatMsg -> {
            if (CollUtil.isNotEmpty(toGroupIds)) {
                toGroupIds.forEach(groupId -> {
                    TioMessage message = new TioMessage(MsgTypeEm.getMstType(chatMsg.getMsgType()), chatMsg.getMsg());
                    message.setChatType(ChatTypeEm.GROUP);
                    message.setSendUserId(chatUser.getId());
                    message.setToUserId(groupId);
                    messageList.add(message);
                });
            }
            if (CollUtil.isNotEmpty(toUserIds)) {
                toUserIds.forEach(toUserId -> {
                    TioMessage message = new TioMessage(MsgTypeEm.getMstType(chatMsg.getMsgType()), chatMsg.getMsg());
                    message.setChatType(ChatTypeEm.SINGLE);
                    message.setSendUserId(chatUser.getId());
                    message.setToUserId(toUserId);
                    messageList.add(message);
                });
            }
        });
        return messageList;
    }

    @Override
    public boolean deleteUserMsg(Integer msgId) {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        ChatMsgUserRel chatMsgUserRel = chatMsgUserRelService.getChatMsgUserRel(chatUserId, msgId);
        if (ObjectUtil.isNull(chatMsgUserRel)) {
            log.error("未查询到聊天消息,chatUserId:{},msgId:{}", chatUserId, msgId);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "未查询到聊天消息");
        }
        return Boolean.TRUE.equals(transactionTemplate.execute(t -> {
            chatRoomService.clearUserLastMsg(chatUserId, msgId);
            chatMsgUserRelService.removeById(chatMsgUserRel.getId());
            return true;
        }));
    }

    @Override
    public boolean revokeMsg(Integer msgId) {
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        Integer chatUserId = chatUser.getId();
        ChatMsg chatMsg = this.get(msgId);
        if (ObjectUtil.isNull(chatMsg)) {
            log.error("未查询到聊天消息,chatUserId:{},msgId:{}", chatUserId, msgId);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "未查询到聊天消息");
        }
        checkMsgCanRevoke(msgId, chatMsg, chatUserId);
        // 发送系统消息
        return Boolean.TRUE.equals(transactionTemplate.execute(t -> {
            boolean ret = this.removeById(msgId);
            if (!ret) {
                log.error("删除消息失败,msgId:{}", msgId);
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "删除消息失败");
            }
            chatMsgUserRelService.clearUserMsgByMsgId(msgId);
            chatRoomService.clearLastMsgByMsgId(msgId);
            // 发送系统消息
            sendSystemMessage(chatUser, chatMsg);
            return true;
        }));
    }

    private void checkMsgCanRevoke(Integer msgId, ChatMsg chatMsg, Integer chatUserId) {
        if (!chatMsg.getSendUserId().equals(chatUserId)) {
            log.error("不能撤回别人的消息,chatUserId:{},msgId:{}", chatUserId, msgId);
            throw new ApiException(ExceptionCodeEm.FORBIDDEN, "不能撤回别人的消息");
        }
        DateTime dateTime = DateUtil.offset(DateUtil.parseDateTime(chatMsg.getSendTime()), DateField.SECOND, 120);
        if (dateTime.compareTo(new Date()) < 0) {
            log.error("只能撤回2分钟内的消息,chatUserId:{},msgId:{}", chatUserId, msgId);
            throw new ApiException(ExceptionCodeEm.FORBIDDEN, "只能撤回2分钟内的消息");
        }
    }

    private void sendSystemMessage(ChatUserResponse chatUser, ChatMsg chatMsg) {
        ChannelContext channelContext = TioUtil.getChannelContext(jrTioConfig.getTioConfig(), chatUser.getId().toString());
        if (channelContext != null) {
            log.info("发送撤回消息系统消息,chatUserId:{},msgId:{}", chatUser.getId(), chatMsg.getId());
            TioMessage message = new TioMessage(MsgTypeEm.EVENT, chatUser.getNickname() + "撤回了一条消息");
            message.setSendUserId(chatUser.getId());
            message.setChatType(ChatTypeEm.getChatType(chatMsg.getChatType()));
            message.setEventType(EventTypeEm.REVOKE_MSG);
            message.setToUserId(chatMsg.getToUserId());
            message.setSendTime(DateUtil.now());
            message.setRevokeId(chatMsg.getId());
            // 发送消息
            eventPublisher.publishEvent(new ChatMsgSendEvent(channelContext, CollUtil.newArrayList(message), "撤回"));
        }
    }

    @Override
    public boolean updateFileMsgStatus(Integer msgId) {
        ChatMsg chatMsg = this.getById(msgId);
        if (!StringUtils.equalsAny(chatMsg.getMsgType(), MsgTypeEm.FILE.name(), MsgTypeEm.IMAGE.name(), MsgTypeEm.VIDEO.name())) {
            return false;
        }
        JSONObject jsonObject = JSON.parseObject(chatMsg.getMsg());
        if (jsonObject.containsKey("status")) {
            jsonObject.put("status", 1);
            ChatMsg updateChatMsg = new ChatMsg();
            updateChatMsg.setId(msgId);
            updateChatMsg.setMsg(jsonObject.toJSONString());
            return this.updateById(updateChatMsg);
        }
        return false;
    }
}
