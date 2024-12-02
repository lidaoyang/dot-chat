package com.dot.msg.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.exception.ApiException;
import com.dot.comm.manager.TokenManager;
import com.dot.msg.chat.dao.ChatRoomDao;
import com.dot.msg.chat.dto.ChatGroupInfoDto;
import com.dot.msg.chat.dto.ChatRoomMsgInfoDto;
import com.dot.msg.chat.dto.ChatRoomUserDto;
import com.dot.msg.chat.dto.ChatUserSimDto;
import com.dot.msg.chat.em.ChatTypeEm;
import com.dot.msg.chat.em.GroupSourceEm;
import com.dot.msg.chat.model.*;
import com.dot.msg.chat.request.ChatRoomAddRequest;
import com.dot.msg.chat.response.*;
import com.dot.msg.chat.service.*;
import com.dot.msg.chat.tio.config.JRTioConfig;
import com.dot.msg.chat.tio.em.MsgTypeEm;
import com.dot.msg.chat.tio.util.TioUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用户聊天室表服务接口实现
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Slf4j
@Service
public class ChatRoomServiceImpl extends ServiceImpl<ChatRoomDao, ChatRoom> implements ChatRoomService {

    @Resource
    private ChatRoomUserRelService chatRoomUserRelService;

    @Resource
    private ChatMsgUserRelService chatMsgUserRelService;

    @Resource
    private ChatFriendApplyUserRelService chatFriendApplyUserRelService;

    @Resource
    private ChatUserService chatUserService;

    @Resource
    private ChatGroupService chatGroupService;

    @Resource
    private ChatGroupMemberService chatGroupMemberService;

    @Resource
    private JRTioConfig jrTioConfig;

    @Resource
    private TokenManager tokenManager;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public String addChatRoom(ChatRoomAddRequest request) {
        ChatRoom chatRoom = getAddChatRoom(request);
        Boolean execute = transactionTemplate.execute(t -> {
            boolean saved = this.save(chatRoom);
            if (saved) {
                List<Integer> userIds = getChatRoomRelUserIds(request);
                boolean added = addChatRoomUserRel(chatRoom.getChatId(), userIds);
                if (!added) {
                    throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "添加聊天室失败");
                }
            }
            return saved;
        });
        if (Boolean.TRUE.equals(execute)) {
            return chatRoom.getChatId();
        }
        return null;
    }

    private List<Integer> getChatRoomRelUserIds(ChatRoomAddRequest request) {
        List<Integer> userIds = new ArrayList<>();
        if (ChatTypeEm.SINGLE.name().equals(request.getChatType())) {
            userIds.add(request.getSendUserId());
            userIds.add(request.getToUserId());
        } else {
            List<Integer> groupMemberIdList = chatGroupMemberService.getChatGroupMemberIdListByGroupId(request.getGroupId());
            if (CollUtil.isNotEmpty(groupMemberIdList)) {
                userIds.addAll(groupMemberIdList);
            }
        }
        return userIds;
    }

    private ChatRoom getAddChatRoom(ChatRoomAddRequest request) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatType(request.getChatType());
        chatRoom.setGroupId(request.getGroupId());
        setChatId(request, chatRoom);
        return chatRoom;
    }

    private void setChatId(ChatRoomAddRequest request, ChatRoom chatRoom) {
        String chatId;
        if (ChatTypeEm.SINGLE.name().equals(request.getChatType())) {
            chatId = TioUtil.generateChatId(request.getSendUserId(), request.getToUserId());
        } else {
            chatId = TioUtil.generateChatId(request.getGroupId());
        }
        chatRoom.setChatId(chatId);
    }

    @Override
    public boolean clearOfflineMsgCount(List<String> chatIds, Integer userId) {
        LambdaUpdateWrapper<ChatRoomUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.in(ChatRoomUserRel::getChatId, chatIds);
        updateWrapper.eq(ChatRoomUserRel::getUserId, userId);
        updateWrapper.set(ChatRoomUserRel::getOfflineMsgCount, 0);
        return chatRoomUserRelService.update(updateWrapper);
    }

    @Override
    public boolean batchUpdateOfflineMsgCount(String chatId, List<Integer> userIds, Integer count) {
        if (CollUtil.isEmpty(userIds)) {
            return false;
        }
        LambdaUpdateWrapper<ChatRoomUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatRoomUserRel::getChatId, chatId);
        updateWrapper.in(ChatRoomUserRel::getUserId, userIds);
        updateWrapper.setSql("offline_msg_count = offline_msg_count + " + count);
        if (count < 0) {
            updateWrapper.apply("offline_msg_count+ {0} >0", count);
        }
        return chatRoomUserRelService.update(updateWrapper);
    }

    @Override
    public boolean clearUnreadMsgCount(String chatId) {
        Integer chatUserId = getChatUserId();
        return clearUnreadMsgCount(chatId, chatUserId);
    }

    @Override
    public boolean clearUnreadMsgCount(String chatId, Integer userId) {
        LambdaUpdateWrapper<ChatRoomUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatRoomUserRel::getChatId, chatId);
        updateWrapper.eq(ObjectUtil.isNotNull(userId), ChatRoomUserRel::getUserId, userId);
        updateWrapper.set(ChatRoomUserRel::getUnreadMsgCount, 0);
        boolean updated = chatRoomUserRelService.update(updateWrapper);
        if (updated) {
            LambdaUpdateWrapper<ChatMsgUserRel> updateWrapper2 = Wrappers.lambdaUpdate();
            updateWrapper2.eq(ChatMsgUserRel::getChatId, chatId);
            updateWrapper2.eq(ChatMsgUserRel::getUserId, userId);
            updateWrapper2.eq(ChatMsgUserRel::getIsUnread, true);
            updateWrapper2.set(ChatMsgUserRel::getIsUnread, false);
            boolean updated1 = chatMsgUserRelService.update(updateWrapper2);
            if (!updated1) {
                log.warn("没有未读消息被更新,chatId:{},userId:{}", chatId, userId);
            }
        }
        return updated;
    }

    @Override
    public boolean updateUnreadMsgCount(String chatId, List<Integer> unreadUserIds, Integer count) {
        if (CollUtil.isEmpty(unreadUserIds)) {
            return false;
        }
        LambdaUpdateWrapper<ChatRoomUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatRoomUserRel::getChatId, chatId);
        updateWrapper.in(ChatRoomUserRel::getUserId, unreadUserIds);
        updateWrapper.setSql("unread_msg_count = unread_msg_count + " + count);
        if (count < 0) {
            updateWrapper.apply("unread_msg_count+ {0} >0", count);
        }
        return chatRoomUserRelService.update(updateWrapper);
    }

    @Override
    public boolean updateLastMsg(ChatMsg chatMsg) {
        String lastMsg = getLastMsg(chatMsg);
        LambdaUpdateWrapper<ChatRoomUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatRoomUserRel::getChatId, chatMsg.getChatId());
        updateWrapper.set(ChatRoomUserRel::getLastMsg, lastMsg);
        updateWrapper.set(ChatRoomUserRel::getLastMsgId, chatMsg.getId());
        updateWrapper.set(ChatRoomUserRel::getLastTime, chatMsg.getSendTime());
        updateWrapper.set(ChatRoomUserRel::getTimestamp, chatMsg.getTimestamp());
        return chatRoomUserRelService.update(updateWrapper);
    }

    @Override
    public boolean clearLastMsgByMsgId(Integer msgId) {
        LambdaUpdateWrapper<ChatRoomUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatRoomUserRel::getLastMsgId, msgId);
        updateWrapper.set(ChatRoomUserRel::getLastMsg, null);
        updateWrapper.set(ChatRoomUserRel::getLastMsgId, null);
        updateWrapper.set(ChatRoomUserRel::getLastTime, null);
        updateWrapper.set(ChatRoomUserRel::getTimestamp, null);
        return chatRoomUserRelService.update(updateWrapper);
    }

    @Override
    public boolean clearUserLastMsg(Integer userId, Integer msgId) {
        LambdaUpdateWrapper<ChatRoomUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatRoomUserRel::getUserId, userId);
        updateWrapper.eq(ChatRoomUserRel::getLastMsgId, msgId);
        updateWrapper.set(ChatRoomUserRel::getLastMsg, null);
        updateWrapper.set(ChatRoomUserRel::getLastMsgId, null);
        updateWrapper.set(ChatRoomUserRel::getLastTime, null);
        updateWrapper.set(ChatRoomUserRel::getTimestamp, null);
        return chatRoomUserRelService.update(updateWrapper);
    }

    private String getLastMsg(ChatMsg chatMsg) {
        String lastMsg;
        if (chatMsg.getMsgType().equals(MsgTypeEm.TEXT.name())) {
            lastMsg = chatMsg.getMsg();
            if (lastMsg.length() > 42) {
                lastMsg = lastMsg.substring(0, 42);
            }
        } else {
            lastMsg = "[" + MsgTypeEm.getMstTypeDesc(chatMsg.getMsgType()) + "]";
        }
        return lastMsg;
    }

    @Override
    public boolean existUserChatRoom(String chatId, Integer userId) {
        LambdaQueryWrapper<ChatRoomUserRel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatRoomUserRel::getId);
        queryWrapper.eq(ChatRoomUserRel::getChatId, chatId);
        queryWrapper.eq(ChatRoomUserRel::getUserId, userId);
        queryWrapper.last("limit 1");
        ChatRoomUserRel one = chatRoomUserRelService.getOne(queryWrapper);
        return ObjectUtil.isNotNull(one);
    }


    @Override
    public boolean addChatRoomUserRel(String chatId, Integer userId) {
        ChatRoomUserRel roomUserRel = getNewChatRoomUserRel(chatId, userId);
        return chatRoomUserRelService.save(roomUserRel);
    }


    @Override
    public boolean addChatRoomUserRel(String chatId, List<Integer> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return false;
        }
        List<ChatRoomUserRel> chatRoomUserRelList = new ArrayList<>();
        userIds.forEach(userId -> chatRoomUserRelList.add(getNewChatRoomUserRel(chatId, userId)));
        return chatRoomUserRelService.saveBatch(chatRoomUserRelList);
    }

    private ChatRoomUserRel getNewChatRoomUserRel(String chatId, Integer userId) {
        ChatRoomUserRel roomUserRel = new ChatRoomUserRel();
        roomUserRel.setChatId(chatId);
        roomUserRel.setUserId(userId);
        roomUserRel.setTimestamp(DateUtil.current());
        return roomUserRel;
    }

    @Override
    public List<ChatRoom> getList(List<String> chatIds) {
        if (CollUtil.isEmpty(chatIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ChatRoom> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(ChatRoom::getChatId, chatIds);
        return this.list(queryWrapper);
    }

    @Override
    public ChatRoom getByChatId(String chatId) {
        LambdaQueryWrapper<ChatRoom> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatRoom::getChatId, chatId);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<ChatRoomUserRel> getChatRoomUserRelListByChatId(String chatId) {
        return chatRoomUserRelService.getChatRoomUserRelListByChatId(chatId);
    }

    @Override
    public List<ChatRoomUserResponse> getChatRoomUserList() {
        Integer chatUserId = getChatUserId();
        // 清除当前聊天id
        TioUtil.removeCurrFriendApplyId(jrTioConfig.getTioConfig(), chatUserId.toString());
        TioUtil.removeCurrChatId(jrTioConfig.getTioConfig(), chatUserId.toString());
        return getChatRoomUserRefreshList();
    }

    @Override
    public List<ChatRoomUserResponse> getChatRoomUserRefreshList() {
        Integer chatUserId = getChatUserId();
        QueryWrapper<ChatRoomUserDto> queryWrapper = Wrappers.query();
        queryWrapper.eq("cru.user_id", chatUserId);
        queryWrapper.orderByDesc("cru.is_top", "cru.timestamp");
        List<ChatRoomUserDto> chatRoomUserList = baseMapper.selectChatRoomUserList(queryWrapper);
        if (CollUtil.isEmpty(chatRoomUserList)) {
            return new ArrayList<>();
        }
        List<Integer> userIdList = new ArrayList<>();
        List<Integer> groupIdList = new ArrayList<>();
        addToList(chatRoomUserList, userIdList, groupIdList);

        List<ChatUserSimDto> chatUserList = getChatUserList(userIdList, chatUserId);
        List<ChatGroup> chatGroupList = getChatGroupList(groupIdList);
        return getRoomUserResponseList(chatRoomUserList, chatUserList, chatGroupList);
    }

    private void addToList(List<ChatRoomUserDto> chatRoomUserList, List<Integer> userIdList,
                           List<Integer> groupIdList) {
        chatRoomUserList.forEach(userRoom -> {
            if (userRoom.getChatType().equals(ChatTypeEm.SINGLE.name())) {
                int toUserId = getToUserId(userRoom.getChatId(), userRoom.getUserId());
                userIdList.add(toUserId);
            } else {
                groupIdList.add(userRoom.getGroupId());
            }
        });
    }

    private int getToUserId(String chatId, Integer userId) {
        int toUserId;
        String[] userIds = chatId.split("_");
        if (userId == Integer.parseInt(userIds[0])) {
            toUserId = Integer.parseInt(userIds[1]);
        } else {
            toUserId = Integer.parseInt(userIds[0]);
        }
        return toUserId;
    }

    private List<ChatGroup> getChatGroupList(List<Integer> groupIdList) {
        List<ChatGroup> chatGroupList = new ArrayList<>();
        if (CollUtil.isNotEmpty(groupIdList)) {
            chatGroupList = chatGroupService.getList(groupIdList);
        }
        return chatGroupList;
    }

    private List<ChatUserSimDto> getChatUserList(List<Integer> userIdList, Integer currentUserId) {
        List<ChatUserSimDto> chatUserList = new ArrayList<>();
        if (CollUtil.isNotEmpty(userIdList)) {
            chatUserList = chatUserService.getChatUserLeftFriendSimList(userIdList, currentUserId);
        }
        return chatUserList;
    }

    private List<ChatRoomUserResponse> getRoomUserResponseList(List<ChatRoomUserDto> chatRoomUserList,
                                                               List<ChatUserSimDto> chatUserList,
                                                               List<ChatGroup> chatGroupList) {
        List<ChatRoomUserResponse> responseList = new ArrayList<>();
        chatRoomUserList.forEach(userRoom -> {
            ChatRoomUserResponse response = BeanUtil.copyProperties(userRoom, ChatRoomUserResponse.class);
            setToTUser(chatUserList, chatGroupList, userRoom, response);
            setLastTime(userRoom, response);
            responseList.add(response);
        });
        return responseList;
    }

    private void setToTUser(List<ChatUserSimDto> chatUserList, List<ChatGroup> chatGroupList, ChatRoomUserDto userRoom,
                            ChatRoomUserResponse response) {
        ChatUserSimResponse toUser = new ChatUserSimResponse();
        if (userRoom.getChatType().equals(ChatTypeEm.SINGLE.name())) {
            int toUserId = getToUserId(userRoom.getChatId(), userRoom.getUserId());
            chatUserList.stream().filter(chatUser -> chatUser.getId() == toUserId).findFirst().ifPresent(chatUser -> {
                toUser.setUserId(chatUser.getId());
                toUser.setAvatar(chatUser.getAvatar());
                toUser.setNickname(chatUser.getNickname());
                toUser.setIsFriend(chatUser.getIsFriend());
                toUser.setIsDissolve(false);
            });
        } else {
            chatGroupList.stream().filter(chatGroup -> chatGroup.getId().equals(userRoom.getGroupId())).findFirst().ifPresent(chatGroup -> {
                toUser.setUserId(chatGroup.getId());
                toUser.setAvatar(chatGroup.getAvatar());
                toUser.setNickname(chatGroup.getName());
                toUser.setIsFriend(false);
                toUser.setIsDissolve(chatGroup.getIsDissolve());
            });
        }
        response.setToUser(toUser);
    }

    private void setLastTime(ChatRoomUserDto userRoom, ChatRoomUserResponse response) {
        if (StringUtils.isNotBlank(userRoom.getLastTime())) {
            String lastDate = userRoom.getLastTime().substring(0, 10);
            String today = DateUtil.today();
            if (lastDate.equals(today)) {
                response.setLastTime(userRoom.getLastTime().substring(11));
            } else {
                response.setLastTime(lastDate.substring(5));
            }
        }
    }

    @Override
    public ChatUnreadCountResponse getChatUnreadCount() {
        ChatUnreadCountResponse response = new ChatUnreadCountResponse();
        Integer chatUserId = getChatUserId();
        long chatUnreadCount = getChatUnreadCount(chatUserId);
        response.setChatUnreadCount(chatUnreadCount);

        long friendApplyUnreadCount = getFriendApplyUnreadCount(chatUserId);
        response.setFriendUnreadCount(friendApplyUnreadCount);
        return response;
    }

    private long getChatUnreadCount(Integer chatUserId) {
        LambdaQueryWrapper<ChatRoomUserRel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatRoomUserRel::getUserId, chatUserId).gt(ChatRoomUserRel::getUnreadMsgCount, 0);
        return chatRoomUserRelService.count(queryWrapper);
    }

    private long getFriendApplyUnreadCount(Integer chatUserId) {
        LambdaQueryWrapper<ChatFriendApplyUserRel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatFriendApplyUserRel::getUserId, chatUserId).gt(ChatFriendApplyUserRel::getUnreadCount, 0);
        return chatFriendApplyUserRelService.count(queryWrapper);
    }

    @Override
    public String gotoSendMsg(Integer friendId) {
        Integer chatUserId = getChatUserId();
        String chatId = TioUtil.generateChatId(chatUserId, friendId);
        boolean existed = existUserChatRoom(chatId, chatUserId);
        if (!existed) {
            saveChatRoomRel(friendId, chatId, chatUserId);
        } else {
            updateChatRoomRelTimestamp(chatId, chatUserId);
        }
        return chatId;
    }

    private void updateChatRoomRelTimestamp(String chatId, Integer chatUserId) {
        LambdaUpdateWrapper<ChatRoomUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatRoomUserRel::getChatId, chatId);
        updateWrapper.eq(ChatRoomUserRel::getUserId, chatUserId);
        updateWrapper.set(ChatRoomUserRel::getTimestamp, DateUtil.current());
        chatRoomUserRelService.update(updateWrapper);
    }

    private void saveChatRoomRel(Integer friendId, String chatId, Integer chatUserId) {
        transactionTemplate.execute(ret -> {
            ChatRoom chatRoom = getByChatId(chatId);
            if (chatRoom == null) {
                chatRoom = new ChatRoom();
                chatRoom.setChatId(chatId);
                chatRoom.setChatType(ChatTypeEm.SINGLE.name());
                boolean saved = this.save(chatRoom);
                if (!saved) {
                    log.error("创建聊天室失败,chatUserId:{},friendId:{}", chatUserId, friendId);
                    throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "创建聊天室失败");
                }
            }
            boolean added = addChatRoomUserRel(chatId, chatUserId);
            if (!added) {
                log.error("添加聊天室用户关系失败,chatUserId:{},friendId:{}", chatUserId, friendId);
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "添加聊天室用户关系失败");
            }
            return true;
        });
    }

    @Override
    public ChatRoomMsgInfoResponse getChatRoomMsgInfo(String chatId) {
        Integer chatUserId = getChatUserId();
        ChatRoomMsgInfoResponse response = new ChatRoomMsgInfoResponse();
        ChatRoomMsgInfoDto chatRoomMsgInfoDto = baseMapper.selectChatRoomMsgInfo(chatId, chatUserId);
        if (Objects.isNull(chatRoomMsgInfoDto)) {
            log.error("聊天室信息不存在,chatUserId:{},chatId:{}", chatUserId, chatId);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "聊天室信息不存在");
        }
        response.setChatId(chatId);
        response.setChatType(chatRoomMsgInfoDto.getChatType());
        response.setGroupId(chatRoomMsgInfoDto.getGroupId());
        response.setMsgNoDisturb(chatRoomMsgInfoDto.getMsgNoDisturb());
        response.setIsTop(chatRoomMsgInfoDto.getIsTop());
        if (ChatTypeEm.GROUP.name().equals(chatRoomMsgInfoDto.getChatType())) {
            ChatGroupInfoDto chatGroupInfo = chatGroupMemberService.getChatGroupInfo(chatRoomMsgInfoDto.getGroupId(), chatUserId);
            if (ObjectUtil.isNull(chatGroupInfo)) {
                log.error("群组信息不存在,chatUserId:{},chatId:{},groupId:{}", chatUserId, chatId, chatRoomMsgInfoDto.getGroupId());
                throw new ApiException(ExceptionCodeEm.NOT_FOUND, "群组信息不存在");
            }
            ChatGroupInfoResponse groupInfo = getChatGroupInfoResponse(chatGroupInfo);
            response.setGroupInfo(groupInfo);
        }
        return response;
    }

    private ChatGroupInfoResponse getChatGroupInfoResponse(ChatGroupInfoDto chatGroupInfo) {
        ChatGroupInfoResponse groupInfo = new ChatGroupInfoResponse();
        groupInfo.setId(chatGroupInfo.getId());
        groupInfo.setAvatar(chatGroupInfo.getAvatar());
        groupInfo.setName(chatGroupInfo.getName());
        groupInfo.setNotice(chatGroupInfo.getNotice());
        groupInfo.setMemberCount(chatGroupInfo.getMemberCount());
        groupInfo.setManagers(chatGroupInfo.getManagers());
        groupInfo.setIsGroupLeader(chatGroupInfo.getIsGroupLeader());
        groupInfo.setIsGroupManager(chatGroupInfo.getIsGroupManager());
        groupInfo.setNickname(chatGroupInfo.getNickname());
        groupInfo.setInviteCfm(chatGroupInfo.getInviteCfm());
        groupInfo.setGroupLeaderId(chatGroupInfo.getGroupLeaderId());
        setSourceDesc(chatGroupInfo, groupInfo);
        return groupInfo;
    }

    private void setSourceDesc(ChatGroupInfoDto chatGroupInfo, ChatGroupInfoResponse groupInfo) {
        if (chatGroupInfo.getSource().equals(GroupSourceEm.INVITE.name())) {
            String nickname = chatUserService.getNicknameById(chatGroupInfo.getInviteUserId());
            groupInfo.setSourceDesc(nickname + GroupSourceEm.INVITE.getDesc());
        } else {
            groupInfo.setSourceDesc(GroupSourceEm.getDesc(chatGroupInfo.getSource()));
        }
    }

    @Override
    public boolean updateMsgNoDisturb(String chatId, Boolean msgNoDisturb) {
        Integer chatUserId = getChatUserId();
        LambdaUpdateWrapper<ChatRoomUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatRoomUserRel::getChatId, chatId)
                .eq(ChatRoomUserRel::getUserId, chatUserId)
                .eq(ChatRoomUserRel::getMsgNoDisturb, !msgNoDisturb)
                .set(ChatRoomUserRel::getMsgNoDisturb, msgNoDisturb);
        return chatRoomUserRelService.update(updateWrapper);
    }

    @Override
    public boolean updateIsTop(String chatId, Boolean isTop) {
        LambdaUpdateWrapper<ChatRoomUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatRoomUserRel::getChatId, chatId)
                .eq(ChatRoomUserRel::getUserId, getChatUserId())
                .eq(ChatRoomUserRel::getIsTop, !isTop)
                .set(ChatRoomUserRel::getIsTop, isTop);
        return chatRoomUserRelService.update(updateWrapper);
    }

    private Integer getChatUserId() {
        return chatUserService.getCurrentChatUserId();
    }

    @Override
    public boolean deleteChatRoomUserRel(String chatId, Integer userId) {
        LambdaQueryWrapper<ChatRoomUserRel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatRoomUserRel::getChatId, chatId)
                .eq(ChatRoomUserRel::getUserId, userId);
        return chatRoomUserRelService.remove(queryWrapper);
    }

    @Override
    public boolean cleanMsgList(String chatId) {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            chatMsgUserRelService.cleanMsgList(chatUserId, chatId);
            cleanLastMsg(chatId, chatUserId);
            return true;
        }));
    }

    private void cleanLastMsg(String chatId, Integer chatUserId) {
        LambdaUpdateWrapper<ChatRoomUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatRoomUserRel::getChatId, chatId)
                .eq(ChatRoomUserRel::getUserId, chatUserId)
                .set(ChatRoomUserRel::getLastMsg, null)
                .set(ChatRoomUserRel::getLastMsgId, null)
                .set(ChatRoomUserRel::getLastTime, null);
        boolean updated = chatRoomUserRelService.update(updateWrapper);
        if (!updated) {
            log.error("清空聊天室消息失败,chatUserId:{},chatId:{}", chatUserId, chatId);
        }
    }

    @Override
    public Boolean deleteChatRoom(String chatId) {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        return transactionTemplate.execute(t -> {
            ChatRoomUserRel chatRoomUserRel = chatRoomUserRelService.getChatRoomUserRel(chatId, chatUserId);
            if (chatRoomUserRel == null) {
                log.warn("聊天室用户关联不存在,chatUserId:{},chatId:{}", chatUserId, chatId);
                throw new ApiException(ExceptionCodeEm.NOT_FOUND, "用户聊天室不存在");
            }
            boolean ret = chatRoomUserRelService.removeById(chatRoomUserRel.getId());
            if (!ret) {
                log.error("删除聊天室用户关联失败,chatUserId:{},chatId:{}", chatUserId, chatId);
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR);
            }
            chatMsgUserRelService.cleanMsgList(chatUserId, chatId);
            cleanLastMsg(chatId, chatUserId);
            return true;
        });
    }
}
