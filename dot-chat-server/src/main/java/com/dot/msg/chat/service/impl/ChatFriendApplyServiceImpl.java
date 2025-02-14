package com.dot.msg.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.exception.ApiException;
import com.dot.msg.chat.dao.ChatFriendApplyDao;
import com.dot.msg.chat.dto.ChatFriendApplyDto;
import com.dot.msg.chat.em.ChatFriendApplyStatusEm;
import com.dot.msg.chat.em.ChatTypeEm;
import com.dot.msg.chat.em.ChatSourceEm;
import com.dot.msg.chat.em.DeviceTypeEm;
import com.dot.msg.chat.model.ChatFriendApply;
import com.dot.msg.chat.model.ChatFriendApplyUserRel;
import com.dot.msg.chat.model.ChatUser;
import com.dot.msg.chat.request.ChatFriendAddRequest;
import com.dot.msg.chat.request.ChatFriendApplyAddRequest;
import com.dot.msg.chat.request.ChatFriendApplyAgreeRequest;
import com.dot.msg.chat.request.ChatFriendApplyReplayRequest;
import com.dot.msg.chat.response.ChatFriendApplyInfoResponse;
import com.dot.msg.chat.response.ChatFriendApplyResponse;
import com.dot.msg.chat.response.ChatUserResponse;
import com.dot.msg.chat.service.ChatFriendApplyService;
import com.dot.msg.chat.service.ChatFriendApplyUserRelService;
import com.dot.msg.chat.service.ChatFriendService;
import com.dot.msg.chat.service.ChatUserService;
import com.dot.msg.chat.tio.config.JRTioConfig;
import com.dot.msg.chat.tio.em.EventTypeEm;
import com.dot.msg.chat.tio.em.MsgTypeEm;
import com.dot.msg.chat.tio.entiy.TioMessage;
import com.dot.msg.chat.tio.service.ChatMsgSendService;
import com.dot.msg.chat.tio.util.TioUtil;
import com.dot.msg.notify.em.NotifyTypeEm;
import com.dot.msg.notify.request.NotifyMsgSendRequest;
import com.dot.msg.notify.response.NotifyMsgResponse;
import com.dot.msg.notify.service.NotifyMsgService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.tio.core.ChannelContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 聊天室新好友申请表服务接口实现
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Slf4j
@Service
public class ChatFriendApplyServiceImpl extends ServiceImpl<ChatFriendApplyDao, ChatFriendApply> implements ChatFriendApplyService {

    @Resource
    private ChatUserService chatUserService;

    @Resource
    private ChatFriendService chatFriendService;

    @Resource
    private ChatFriendApplyUserRelService chatFriendApplyUserRelService;

    @Resource
    private NotifyMsgService notifyMsgService;

    @Resource
    private ChatMsgSendService chatMsgSendService;

    @Resource
    private JRTioConfig jrTioConfig;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public List<ChatFriendApplyResponse> getChatFriendApplyList() {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        List<ChatFriendApplyDto> friendApplyList = baseMapper.selectChatFriendApplyList(chatUserId);
        if (CollUtil.isEmpty(friendApplyList)) {
            return Collections.emptyList();
        }
        List<ChatFriendApplyResponse> responseList = new ArrayList<>();
        friendApplyList.forEach(friendApply -> {
            ChatFriendApplyResponse response = BeanUtil.copyProperties(friendApply, ChatFriendApplyResponse.class);
            setReason(friendApply, chatUserId, response);
            if (StringUtils.isNotBlank(friendApply.getRemark())) {
                response.setNickname(friendApply.getRemark());
            }
            responseList.add(response);
        });
        TioUtil.removeCurrChatId(jrTioConfig.getTioConfig(), chatUserId.toString());
        TioUtil.removeCurrFriendApplyId(jrTioConfig.getTioConfig(), chatUserId.toString());
        return responseList;
    }

    private void setReason(ChatFriendApplyDto friendApply, Integer chatUserId, ChatFriendApplyResponse response) {
        if (chatUserId.equals(friendApply.getApplyUserId())) {
            if (StringUtils.isBlank(friendApply.getApplyReply())) {
                response.setReason("我: " + friendApply.getApplyReason());
            } else {
                response.setReason(friendApply.getNickname() + ": " + friendApply.getApplyReply());
            }
        } else {
            if (StringUtils.isBlank(friendApply.getApplyReply())) {
                response.setReason(friendApply.getNickname() + ": " + friendApply.getApplyReason());
            } else {
                response.setReason("我: " + friendApply.getApplyReply());
            }
        }
    }

    @Override
    public ChatFriendApplyInfoResponse getChatFriendApplyInfo(Integer applyId) {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        ChatFriendApplyDto friendApply = baseMapper.selectChatFriendApplyInfo(applyId, chatUserId);
        if (ObjectUtil.isNull(friendApply)) {
            log.error("好友申请详情不存在,applyId:{},chatUserId:{}", applyId, chatUserId);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "好友申请详情不存在");
        }

        ChatFriendApplyInfoResponse response = BeanUtil.copyProperties(friendApply, ChatFriendApplyInfoResponse.class);
        String sourceDesc = getSourceDesc(friendApply, chatUserId);
        response.setSourceDesc(sourceDesc);
        List<NotifyMsgResponse> notifyMsgList = notifyMsgService.getFriendApplyNotifyMsgList(applyId.toString(), chatUserId, friendApply.getNickname());
        response.setNotifyMsgList(notifyMsgList);
        if (friendApply.getUnreadCount() > 0) {
            // 更新未读消息
            clearUnreadCount(applyId, chatUserId);
        }
        // 设置当前用户选中的好友申请ID
        TioUtil.setCurrFriendApplyId(jrTioConfig.getTioConfig(), chatUserId.toString(), friendApply.getId());
        return response;
    }

    private String getSourceDesc(ChatFriendApplyDto friendApply, Integer chatUserId) {
        String sourceDesc = ChatSourceEm.getDesc(friendApply.getSource());
        if (chatUserId.equals(friendApply.getFriendId())) {
            sourceDesc = "对方" + sourceDesc;
        }
        return sourceDesc;
    }

    @Override
    public boolean addChatFriendApply(ChatFriendApplyAddRequest request) {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        request.setChatUserId(chatUserId);
        Integer applyId = addChatFriendApplyRetApplyId(request);
        return ObjectUtil.isNotNull(applyId);
    }

    @Override
    public Integer addChatFriendApplyRetApplyId(ChatFriendApplyAddRequest request) {
        ChatFriendApply chatFriendApply = getAddChatFriendApply(request);
        transactionTemplate.execute(t -> {
            boolean ret = saveOrUpdateFriendApply(chatFriendApply);
            if (ret) {
                // 保存好友申请关联表
                saveFriendApplyRel(chatFriendApply, request);
                // 发送新好友申请通知消息
                sendFriendApplyNotify(chatFriendApply);
            }
            return ret;
        });
        // 设置当前用户选中的好友申请ID
        TioUtil.setCurrFriendApplyId(jrTioConfig.getTioConfig(), chatFriendApply.getApplyUserId().toString(), chatFriendApply.getId());
        return chatFriendApply.getId();
    }

    private ChatFriendApply getAddChatFriendApply(ChatFriendApplyAddRequest request) {
        ChatFriendApply chatFriendApply = new ChatFriendApply();
        chatFriendApply.setApplyUserId(request.getChatUserId());
        chatFriendApply.setFriendId(request.getFriendId());
        chatFriendApply.setStatus(ChatFriendApplyStatusEm.APPLYING.getStatus());
        chatFriendApply.setSource(request.getSource());
        chatFriendApply.setApplyReason(request.getApplyReason());
        return chatFriendApply;
    }

    private boolean saveOrUpdateFriendApply(ChatFriendApply chatFriendApply) {
        ChatFriendApply oldChatFriendApply = getChatFriendApply(chatFriendApply.getApplyUserId(), chatFriendApply.getFriendId());
        if (ObjectUtil.isNotNull(oldChatFriendApply)) {
            chatFriendApply.setId(oldChatFriendApply.getId());
            chatFriendApply.setApplyReply("");
        }
        boolean ret;
        if (ObjectUtil.isNotNull(chatFriendApply.getId())) {
            ret = this.updateById(chatFriendApply);
        } else {
            ret = this.save(chatFriendApply);
        }
        return ret;
    }

    private ChatFriendApply getChatFriendApply(Integer applyUserId, Integer friendId) {
        LambdaQueryWrapper<ChatFriendApply> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.and(wrapper -> {
            wrapper.eq(ChatFriendApply::getApplyUserId, applyUserId)
                    .eq(ChatFriendApply::getFriendId, friendId);
        }).or(wrapper -> {
            wrapper.eq(ChatFriendApply::getApplyUserId, friendId)
                    .eq(ChatFriendApply::getFriendId, applyUserId);
        });
        return this.getOne(queryWrapper);
    }

    private void saveFriendApplyRel(ChatFriendApply chatFriendApply, ChatFriendApplyAddRequest request) {
        List<ChatFriendApplyUserRel> chatFriendApplyUserRelList = getAddChatFriendApplyUserRelList(chatFriendApply, request);
        if (CollUtil.isEmpty(chatFriendApplyUserRelList)) {
            log.error("聊天室新好友申请关联已存在不用添加,chatFriendApply:{}", JSON.toJSONString(chatFriendApply));
            return;
        }
        boolean saved1 = chatFriendApplyUserRelService.saveBatch(chatFriendApplyUserRelList);
        if (!saved1) {
            log.error("保存聊天室新好友申请关联表失败,chatFriendApplyUserRelList:{}", JSON.toJSONString(chatFriendApplyUserRelList));
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "保存聊天室新好友申请失败");
        }
    }

    private List<ChatFriendApplyUserRel> getAddChatFriendApplyUserRelList(ChatFriendApply chatFriendApply,
                                                                          ChatFriendApplyAddRequest request) {
        List<ChatFriendApplyUserRel> chatFriendApplyUserRelList = new ArrayList<>();
        List<ChatFriendApplyUserRel> applyUserRelList = getChatFriendApplyUserRelList(chatFriendApply.getId());
        if (CollUtil.isEmpty(applyUserRelList)) {
            ChatFriendApplyUserRel addApplyUserRel = getAddApplyUserRel(chatFriendApply.getId(), chatFriendApply.getApplyUserId(), chatFriendApply.getFriendId(), request.getRemark(), request.getLabel());
            chatFriendApplyUserRelList.add(addApplyUserRel);
            chatFriendApplyUserRelList.add(getAddApplyUserRel(chatFriendApply.getId(), chatFriendApply.getFriendId(), chatFriendApply.getApplyUserId()));
            return chatFriendApplyUserRelList;
        }
        Optional<ChatFriendApplyUserRel> first = applyUserRelList.stream().filter(applyRel -> applyRel.getUserId().equals(chatFriendApply.getApplyUserId())).findFirst();
        if (first.isEmpty()) { // 不存在关联记录就是添加
            ChatFriendApplyUserRel addApplyUserRel = getAddApplyUserRel(chatFriendApply.getId(), chatFriendApply.getApplyUserId(), chatFriendApply.getFriendId(), request.getRemark(), request.getLabel());
            chatFriendApplyUserRelList.add(addApplyUserRel);
        } else {
            ChatFriendApplyUserRel applyUserRel = first.get();
            applyUserRel.setRemark(request.getRemark());
            applyUserRel.setLabel(request.getLabel());
            chatFriendApplyUserRelService.updateById(applyUserRel);
        }
        Optional<ChatFriendApplyUserRel> first2 = applyUserRelList.stream().filter(applyRel -> applyRel.getUserId().equals(chatFriendApply.getFriendId())).findFirst();
        if (first2.isEmpty()) {
            chatFriendApplyUserRelList.add(getAddApplyUserRel(chatFriendApply.getId(), chatFriendApply.getFriendId(), chatFriendApply.getApplyUserId()));
        } else {
            ChatFriendApplyUserRel applyUserRel = first2.get();
            applyUserRel.setUnreadCount(applyUserRel.getUnreadCount() + 1);
            chatFriendApplyUserRelService.updateById(applyUserRel);
        }
        return chatFriendApplyUserRelList;
    }

    private ChatFriendApplyUserRel getAddApplyUserRel(Integer applyId, Integer userId, Integer friendId) {
        ChatFriendApplyUserRel applyUserRel = new ChatFriendApplyUserRel();
        applyUserRel.setApplyId(applyId);
        applyUserRel.setUserId(userId);
        applyUserRel.setFriendId(friendId);
        applyUserRel.setUnreadCount(1);
        return applyUserRel;
    }

    private ChatFriendApplyUserRel getAddApplyUserRel(Integer applyId, Integer userId, Integer friendId, String remark,
                                                      String label) {
        ChatFriendApplyUserRel applyUserRel = new ChatFriendApplyUserRel();
        applyUserRel.setApplyId(applyId);
        applyUserRel.setUserId(userId);
        applyUserRel.setFriendId(friendId);
        applyUserRel.setUnreadCount(0);
        applyUserRel.setRemark(remark);
        applyUserRel.setLabel(label);
        return applyUserRel;
    }

    private List<ChatFriendApplyUserRel> getChatFriendApplyUserRelList(Integer applyId) {
        LambdaQueryWrapper<ChatFriendApplyUserRel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatFriendApplyUserRel::getApplyId, applyId);
        return chatFriendApplyUserRelService.list(queryWrapper);
    }

    private void sendFriendApplyNotify(ChatFriendApply chatFriendApply) {
        sendFriendApplyNotify(chatFriendApply.getId(), chatFriendApply.getApplyUserId(), chatFriendApply.getFriendId(), chatFriendApply.getApplyReason());
    }

    @Override
    public boolean replayFriendApply(ChatFriendApplyReplayRequest request) {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        ChatFriendApply friendApply = this.getById(request.getApplyId());
        checkUserAuth(request.getApplyId(), friendApply, chatUserId);

        ChatFriendApply newFriendApply = new ChatFriendApply();
        newFriendApply.setId(request.getApplyId());
        Integer toUserId;
        if (chatUserId.equals(friendApply.getApplyUserId())) {
            toUserId = friendApply.getFriendId();
            newFriendApply.setApplyReason(request.getReplayContent());
        } else {
            toUserId = friendApply.getApplyUserId();
            newFriendApply.setApplyReply(request.getReplayContent());
        }
        // 设置当前用户选中的好友申请ID
        TioUtil.setCurrFriendApplyId(jrTioConfig.getTioConfig(), chatUserId.toString(), request.getApplyId());
        Boolean executed = transactionTemplate.execute(t -> {
            boolean updated = this.updateById(newFriendApply);
            if (updated) {
                // 更新未读数
                updateChatFriendApplyRelUnreadCount(request.getApplyId(), toUserId);
                // 发送通知
                sendFriendApplyNotify(request.getApplyId(), chatUserId, toUserId, request.getReplayContent());
            }
            return updated;
        });
        return Boolean.TRUE.equals(executed);
    }

    private void updateChatFriendApplyRelUnreadCount(Integer applyId, Integer userId) {
        Integer friendApplyId = TioUtil.getCurrFriendApplyId(jrTioConfig.getTioConfig(), userId.toString());
        if (friendApplyId != null && friendApplyId.equals(applyId)) {
            log.info("申请好友处于会话状态不更新未读数:{}", friendApplyId);
            return;
        }
        LambdaUpdateWrapper<ChatFriendApplyUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatFriendApplyUserRel::getApplyId, applyId);
        updateWrapper.eq(ChatFriendApplyUserRel::getUserId, userId);
        updateWrapper.setSql("unread_count=unread_count+1");
        boolean ret = chatFriendApplyUserRelService.update(updateWrapper);
        if (!ret) {
            log.error("更新好友申请未读数失败,applyId:{},userId:{}", applyId, userId);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "更新好友申请未读数失败");
        }
    }

    private void sendFriendApplyNotify(Integer applyId, Integer sendUserId, Integer toUserId, String content) {
        sendFriendApplyNotify(applyId, sendUserId, toUserId, EventTypeEm.FRIEND_APPLY, content);
    }

    private void sendFriendApplyAgreeNotify(Integer applyId, Integer sendUserId, Integer toUserId) {
        sendFriendApplyNotify(applyId, sendUserId, toUserId, EventTypeEm.FRIEND_AGREE, "同意");
    }

    private void sendFriendApplyNotify(Integer applyId, Integer sendUserId, Integer toUserId, EventTypeEm eventType,
                                       String content) {
        NotifyMsgSendRequest msgSendRequest = new NotifyMsgSendRequest();
        msgSendRequest.setLinkid(applyId.toString());
        msgSendRequest.setSendUserId(sendUserId);
        msgSendRequest.setToUserId(toUserId);
        msgSendRequest.setNotifyType(NotifyTypeEm.USER);
        msgSendRequest.setMsgType(MsgTypeEm.EVENT);
        msgSendRequest.setEventType(eventType);
        msgSendRequest.setMsgContent(content);
        msgSendRequest.setSendTime(DateUtil.now());
        boolean sent = notifyMsgService.sendNotifyMsg(msgSendRequest);
        if (!sent) {
            log.error("发送新好友申请通知消息失败,request:{}", JSON.toJSONString(msgSendRequest));
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "发送新好友申请通知消息失败");
        }
    }

    private void checkUserAuth(Integer applyId, ChatFriendApply friendApply, Integer chatUserId) {
        if (ObjectUtil.isNull(friendApply)) {
            log.error("聊天室新好友申请不存在,applyId:{}", applyId);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "聊天室新好友申请不存在");
        }
        if (!chatUserId.equals(friendApply.getApplyUserId()) && !chatUserId.equals(friendApply.getFriendId())) {
            log.error("当前用户不是聊天室新好友申请的申请人或好友,applyId:{}", applyId);
            throw new ApiException(ExceptionCodeEm.FORBIDDEN, "当前用户不是聊天室新好友申请的申请人或好友");
        }
    }

    @Override
    public String agreeFriendApply(ChatFriendApplyAgreeRequest request) {
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        return agreeFriendApplyRetChatId(request, chatUser);
    }

    @Override
    public String agreeFriendApplyRetChatId(ChatFriendApplyAgreeRequest request, ChatUserResponse chatUser) {
        Integer chatUserId = chatUser.getId();
        ChatFriendApply friendApply = this.getById(request.getApplyId());
        if (!chatUserId.equals(friendApply.getFriendId())) {
            log.error("当前用户不是聊天室新好友申请的好友,applyId:{},chatUserId:{}", request.getApplyId(), chatUserId);
            throw new ApiException(ExceptionCodeEm.FORBIDDEN, "当前用户不是聊天室新好友申请的好友");
        }
        String chatId = TioUtil.generateChatId(chatUserId, friendApply.getApplyUserId());
        if (friendApply.getStatus() == ChatFriendApplyStatusEm.AGREE.getStatus()) {
            log.error("聊天室新好友申请已同意,applyId:{}", request.getApplyId());
            return chatId;
        }
        TioUtil.setCurrFriendApplyId(jrTioConfig.getTioConfig(), chatUserId.toString(), request.getApplyId());
        Boolean executed = transactionTemplate.execute(t -> {
            boolean updated = updateFriendApplyStatus(request.getApplyId());
            if (updated) {
                // 添加好友
                ChatFriendAddRequest chatFriendAddRequest = getChatFriendAddRequest(friendApply, chatUser.getNickname(), request);
                addChatFriend(request.getApplyId(), chatFriendAddRequest);
                // 发送系统消息
                sendSystemToUser(request, chatUserId, friendApply);
                // 发送同意通知
                sendFriendApplyAgreeNotify(request.getApplyId(), chatUserId, friendApply.getApplyUserId());
            }
            return updated;
        });
        return Boolean.TRUE.equals(executed) ? chatId : null;
    }

    private void sendSystemToUser(ChatFriendApplyAgreeRequest request, Integer chatUserId, ChatFriendApply friendApply) {
        ChannelContext channelContext = TioUtil.getChannelContext(jrTioConfig.getTioConfig(), chatUserId.toString());
        if (channelContext == null) {
            channelContext = TioUtil.getChannelContext(jrTioConfig.getTioConfig(), friendApply.getApplyUserId().toString());
        }
        if (channelContext != null) {
            TioMessage message = getMessage(request, chatUserId, friendApply);
            chatMsgSendService.sendAndSaveMsg(channelContext, message);
        }
    }

    private TioMessage getMessage(ChatFriendApplyAgreeRequest request, Integer chatUserId,
                                  ChatFriendApply friendApply) {
        String nickname = StringUtils.defaultIfBlank(request.getRemark(), request.getNickname());
        String msgContent = "你已添加了" + nickname + "，现在可以开始聊天了。";
        TioMessage message = new TioMessage(MsgTypeEm.SYSTEM, msgContent);
        message.setSendUserId(chatUserId);
        message.setChatType(ChatTypeEm.SINGLE);
        message.setToUserId(friendApply.getApplyUserId());
        message.setSendTime(DateUtil.now());
        message.setDeviceType(DeviceTypeEm.SYS.name());
        return message;
    }

    private boolean updateFriendApplyStatus(Integer applyId) {
        LambdaUpdateWrapper<ChatFriendApply> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatFriendApply::getId, applyId)
                .set(ChatFriendApply::getStatus, ChatFriendApplyStatusEm.AGREE.getStatus());
        return this.update(updateWrapper);
    }

    private ChatFriendAddRequest getChatFriendAddRequest(ChatFriendApply friendApply, String nickname,
                                                         ChatFriendApplyAgreeRequest applyAgreeRequest) {
        ChatFriendAddRequest chatFriendAddRequest = new ChatFriendAddRequest();
        chatFriendAddRequest.setApplyUserId(friendApply.getApplyUserId());
        chatFriendAddRequest.setFriendId(friendApply.getFriendId());
        chatFriendAddRequest.setNickname(nickname);
        chatFriendAddRequest.setSource(friendApply.getSource());
        ChatFriendApplyUserRel applyUserRel = getFriendApplyUserRel(friendApply.getId(), friendApply.getApplyUserId());
        chatFriendAddRequest.setRemark(applyUserRel.getRemark());
        chatFriendAddRequest.setLabel(applyUserRel.getLabel());
        if (StringUtils.isBlank(applyAgreeRequest.getNickname())){
            ChatUser applyChatUser = chatUserService.getById(friendApply.getApplyUserId());
            applyAgreeRequest.setNickname(applyChatUser.getNickname());
        }
        chatFriendAddRequest.setApplyAgreeRequest(applyAgreeRequest);
        return chatFriendAddRequest;
    }

    private ChatFriendApplyUserRel getFriendApplyUserRel(Integer applyId, Integer userId) {
        LambdaQueryWrapper<ChatFriendApplyUserRel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatFriendApplyUserRel::getId, ChatFriendApplyUserRel::getRemark, ChatFriendApplyUserRel::getLabel);
        queryWrapper.eq(ChatFriendApplyUserRel::getApplyId, applyId);
        queryWrapper.eq(ChatFriendApplyUserRel::getUserId, userId);
        return chatFriendApplyUserRelService.getOne(queryWrapper);
    }

    private void addChatFriend(Integer applyId, ChatFriendAddRequest chatFriendAddRequest) {
        boolean added = chatFriendService.addChatFriend(chatFriendAddRequest);
        if (!added) {
            log.error("添加好友失败,applyId:{}", applyId);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "添加好友失败");
        }
    }

    @Override
    public boolean deleteFriendApply(Integer applyId) {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        LambdaQueryWrapper<ChatFriendApplyUserRel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatFriendApplyUserRel::getApplyId, applyId);
        queryWrapper.eq(ChatFriendApplyUserRel::getUserId, chatUserId);
        return chatFriendApplyUserRelService.remove(queryWrapper);
    }

    @Override
    public boolean clearUnreadCount(Integer applyId) {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        return clearUnreadCount(applyId, chatUserId);
    }

    @Override
    public boolean clearUnreadCount(Integer applyId, Integer userId) {
        return Boolean.TRUE.equals(transactionTemplate.execute(t -> {
            boolean ret = clearApplyUnReadCount(applyId, userId);
            if (!ret) {
                log.warn("没有未读消息数,applyId:{},chatUserId:{}", applyId, userId);
            }
            boolean ret2 = notifyMsgService.updateIsRead(applyId.toString(), userId);
            if (!ret2) {
                log.warn("未更新未读消息,applyId:{},chatUserId:{}", applyId, userId);
            }
            return true;
        }));
    }

    private boolean clearApplyUnReadCount(Integer applyId, Integer userId) {
        LambdaUpdateWrapper<ChatFriendApplyUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatFriendApplyUserRel::getApplyId, applyId);
        updateWrapper.eq(ChatFriendApplyUserRel::getUserId, userId);
        updateWrapper.gt(ChatFriendApplyUserRel::getUnreadCount, 0);
        updateWrapper.set(ChatFriendApplyUserRel::getUnreadCount, 0);
        return chatFriendApplyUserRelService.update(updateWrapper);
    }
}
