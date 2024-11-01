package com.dot.msg.notify.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.comm.constants.TioConstant;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.entity.LoginUsername;
import com.dot.comm.exception.ApiException;
import com.dot.comm.manager.TokenManager;
import com.dot.msg.chat.model.ChatUser;
import com.dot.msg.chat.service.ChatUserService;
import com.dot.msg.chat.tio.config.JRTioConfig;
import com.dot.msg.chat.tio.em.EventTypeEm;
import com.dot.msg.chat.tio.em.MsgTypeEm;
import com.dot.msg.chat.tio.entiy.TioMessage;
import com.dot.msg.chat.tio.util.TioUtil;
import com.dot.msg.notify.dao.NotifyMsgDao;
import com.dot.msg.notify.dto.NotifyMsgDto;
import com.dot.msg.notify.dto.NotifyMsgInfoDto;
import com.dot.msg.notify.em.NotifyBizEm;
import com.dot.msg.notify.em.NotifyTypeEm;
import com.dot.msg.notify.model.NotifyMsg;
import com.dot.msg.notify.model.NotifyMsgUserRel;
import com.dot.msg.notify.request.EntNotifyMsgSendRequest;
import com.dot.msg.notify.request.NotifyMsgSendRequest;
import com.dot.msg.notify.response.EntNotifyMsgResponse;
import com.dot.msg.notify.response.NotifyMsgContentResponse;
import com.dot.msg.notify.response.NotifyMsgResponse;
import com.dot.msg.notify.service.NotifyMsgService;
import com.dot.msg.notify.service.NotifyMsgUserRelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.tio.core.Tio;
import org.tio.websocket.common.WsResponse;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 通知消息服务接口实现
 *
 * @author Dao-yang
 * @date: 2024-01-30 14:44:18
 */
@Slf4j
@Service
public class NotifyMsgServiceImpl extends ServiceImpl<NotifyMsgDao, NotifyMsg> implements NotifyMsgService {

    @Resource
    private ChatUserService chatUserService;

    @Resource
    private NotifyMsgUserRelService notifyMsgUserRelService;

    @Resource
    private TokenManager tokenManager;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private JRTioConfig jrTioConfig;


    @Override
    public List<NotifyMsgResponse> getFriendApplyNotifyMsgList(String linkid, Integer userId, String nickname) {
        List<NotifyMsgDto> notifyMsgDtos = this.getFriendApplyNotifyMsgList(linkid, userId);
        if (CollUtil.isEmpty(notifyMsgDtos)) {
            return new ArrayList<>(0);
        }
        List<NotifyMsgResponse> responseList = new ArrayList<>();
        notifyMsgDtos.forEach(notifyMsgDto -> {
            NotifyMsgResponse response = BeanUtil.copyProperties(notifyMsgDto, NotifyMsgResponse.class);
            if (userId.equals(notifyMsgDto.getSendUserId())) {
                response.setNickname("我");
            } else {
                response.setNickname(nickname);
            }
            responseList.add(response);
        });
        return responseList;
    }

    @Override
    public List<NotifyMsgDto> getFriendApplyNotifyMsgList(String linkid, Integer userId) {
        List<NotifyMsgDto> notifyMsgList = getNotifyMsgList(linkid, userId, MsgTypeEm.EVENT, EventTypeEm.FRIEND_APPLY, 3);
        Collections.reverse(notifyMsgList);
        return notifyMsgList;
    }

    @Override
    public List<EntNotifyMsgResponse> getNotifyMsgNotifyList(Integer limit) {
        LoginUsername loginUser = tokenManager.getLoginUser();
        ChatUser chatUser = chatUserService.getChatUser(loginUser.getUid());
        if (ObjectUtil.isNull(chatUser)) {
            log.warn("用户没有存在聊天用户里,account:{}", loginUser.getAccount());
            return new ArrayList<>(0);
        }
        List<EntNotifyMsgResponse> responseList = new ArrayList<>();
        List<NotifyMsgDto> notifyMsgList = baseMapper.selectUnReadNotifyMsgList(NotifyBizEm.BIZ_ORDER.getBizId(chatUser.getId()), chatUser.getId(), MsgTypeEm.NOTICE.name(), EventTypeEm.BIZ_ORDER.name(), limit);
        notifyMsgList.forEach(notifyMsgDto -> {
            EntNotifyMsgResponse response = BeanUtil.copyProperties(notifyMsgDto, EntNotifyMsgResponse.class, "msgContent");
            response.setMsgContent(JSON.parseObject(notifyMsgDto.getMsgContent(), NotifyMsgContentResponse.class));
            responseList.add(response);
        });
        return responseList;
    }

    @Override
    public List<NotifyMsgDto> getNotifyMsgList(String linkid, Integer userId, MsgTypeEm msgType, EventTypeEm eventType, Integer limit) {
        return baseMapper.selectNotifyMsgList(linkid, userId, msgType.name(), eventType.name(), limit);
    }

    @Override
    public boolean sendEntNotifyMsg(EntNotifyMsgSendRequest sendRequest) {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        NotifyMsgSendRequest msgSendRequest = new NotifyMsgSendRequest();
        msgSendRequest.setLinkid(sendRequest.getNotifyBiz().getBizId(sendRequest.getEnterpriseId()));
        msgSendRequest.setSendUserId(chatUserId);
        msgSendRequest.setToUserId(sendRequest.getEnterpriseId());
        msgSendRequest.setNotifyType(NotifyTypeEm.BIZ);
        msgSendRequest.setMsgType(MsgTypeEm.NOTICE);
        msgSendRequest.setNotifyBiz(sendRequest.getNotifyBiz());
        msgSendRequest.setEventType(EventTypeEm.getEventType(sendRequest.getNotifyBiz().name()));
        msgSendRequest.setMsgContent(JSON.toJSONString(sendRequest.getMsgContent()));
        msgSendRequest.setSendTime(DateUtil.now());
        boolean ret = sendNotifyMsg(msgSendRequest);
        if (!ret) {
            log.error("发送失败,chatUserId:{},sendRequest:{}", chatUserId, sendRequest);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "发送失败");
        }
        return true;
    }


    @Override
    public boolean sendNotifyMsg(NotifyMsgSendRequest request) {
        boolean added = addNotifyMsg(request);
        if (added) {
            sendNotify(request);
        }
        return added;
    }

    @Override
    public boolean addNotifyMsg(NotifyMsgSendRequest request) {
        Boolean executed = transactionTemplate.execute(t -> {
            NotifyMsg notifyMsg = getNotifyMsg(request);
            boolean saved = this.save(notifyMsg);
            if (saved) {
                if (request.getNotifyType() == NotifyTypeEm.USER) {
                    saveUserNotify(request, notifyMsg);
                } else if (request.getNotifyType() == NotifyTypeEm.BIZ) {
                    saveBizNotifyUser(request, notifyMsg);
                }
            }
            return saved;
        });
        return Boolean.TRUE.equals(executed);
    }

    private NotifyMsg getNotifyMsg(NotifyMsgSendRequest request) {
        NotifyMsg notifyMsg = new NotifyMsg();
        notifyMsg.setLinkid(request.getLinkid());
        notifyMsg.setMsgType(request.getMsgType().name());
        notifyMsg.setEventType(request.getEventType().name());
        notifyMsg.setMsgContent(request.getMsgContent());
        notifyMsg.setSendTime(request.getSendTime());
        notifyMsg.setUserId(request.getToUserId());
        notifyMsg.setNotifyType(request.getNotifyType().getType());
        return notifyMsg;
    }

    private void saveUserNotify(NotifyMsgSendRequest request, NotifyMsg notifyMsg) {
        List<NotifyMsgUserRel> notifyMsgUserRelList = getNotifyMsgUserRel(request, notifyMsg);
        boolean saved1 = notifyMsgUserRelService.saveBatch(notifyMsgUserRelList);
        if (!saved1) {
            log.error("保存通知用户信息失败,request:{}", JSON.toJSONString(request));
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "保存通知用户信息失败");
        }
    }

    private void saveBizNotifyUser(NotifyMsgSendRequest request, NotifyMsg notifyMsg) {
        List<NotifyMsgUserRel> notifyMsgUserRelList = getNotifyMsgUserRelList(request, notifyMsg);
        boolean saved2 = notifyMsgUserRelService.saveBatch(notifyMsgUserRelList);
        if (!saved2) {
            log.error("保存通知用户信息失败,request:{}", JSON.toJSONString(request));
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "保存通知用户信息失败");
        }
    }

    private List<NotifyMsgUserRel> getNotifyMsgUserRelList(NotifyMsgSendRequest request, NotifyMsg notifyMsg) {
        List<NotifyMsgUserRel> notifyMsgUserRelList = new ArrayList<>();
        List<Integer> chatUserIds = chatUserService.getAllUserIds();
        if (CollectionUtil.isEmpty(chatUserIds)) {
            log.error("没有找到聊天用户,request:{}", JSON.toJSONString(request));
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "没有找到聊天用户");
        }
        chatUserIds.forEach(userId -> {
            NotifyMsgUserRel notifyMsgUserRel = new NotifyMsgUserRel();
            notifyMsgUserRel.setUserId(userId);
            notifyMsgUserRel.setNotifyMsgId(notifyMsg.getId());
            notifyMsgUserRel.setLinkid(request.getLinkid());
            notifyMsgUserRel.setSendUserId(request.getSendUserId());
            notifyMsgUserRel.setToUserId(request.getToUserId());
            notifyMsgUserRelList.add(notifyMsgUserRel);
        });
        return notifyMsgUserRelList;
    }

    private List<NotifyMsgUserRel> getNotifyMsgUserRel(NotifyMsgSendRequest request, NotifyMsg notifyMsg) {
        List<NotifyMsgUserRel> notifyMsgUserRelList = new ArrayList<>();
        NotifyMsgUserRel notifyMsgUserRel = getMsgUserRel(request.getSendUserId(), request, notifyMsg);
        notifyMsgUserRelList.add(notifyMsgUserRel);
        NotifyMsgUserRel notifyMsgUserRel2 = getMsgUserRel(request.getToUserId(), request, notifyMsg);
        notifyMsgUserRelList.add(notifyMsgUserRel2);
        return notifyMsgUserRelList;
    }

    private NotifyMsgUserRel getMsgUserRel(Integer userId, NotifyMsgSendRequest request, NotifyMsg notifyMsg) {
        NotifyMsgUserRel notifyMsgUserRel = new NotifyMsgUserRel();
        notifyMsgUserRel.setUserId(userId);
        notifyMsgUserRel.setNotifyMsgId(notifyMsg.getId());
        notifyMsgUserRel.setLinkid(request.getLinkid());
        notifyMsgUserRel.setSendUserId(request.getSendUserId());
        notifyMsgUserRel.setToUserId(request.getToUserId());
        notifyMsgUserRel.setIsOffline(TioUtil.isOffline(jrTioConfig.getTioConfig(), request.getToUserId().toString()));
        if (EventTypeEm.FRIEND_APPLY.name().equals(notifyMsg.getEventType())) {
            Integer friendApplyId = TioUtil.getCurrFriendApplyId(jrTioConfig.getTioConfig(), request.getToUserId().toString());
            if (friendApplyId != null && friendApplyId.toString().equals(request.getLinkid())) {
                notifyMsgUserRel.setIsRead(true);
            }
        }
        return notifyMsgUserRel;
    }

    private void sendNotify(NotifyMsgSendRequest request) {
        if (request.getNotifyType() == NotifyTypeEm.USER) {
            boolean online = TioUtil.isOnline(jrTioConfig.getTioConfig(), request.getToUserId().toString());
            if (online) {
                TioMessage message = getTioMessage(request);
                WsResponse meResponse = WsResponse.fromText(message.toString(), TioConstant.CHARSET);
                Tio.sendToUser(jrTioConfig.getTioConfig(), message.getToUserId().toString(), meResponse);
            }
        } else if (request.getNotifyType() == NotifyTypeEm.BIZ) {
            TioMessage message = getTioMessage(request);
            WsResponse meResponse = WsResponse.fromText(message.toString(), TioConstant.CHARSET);
            Tio.sendToBsId(jrTioConfig.getTioConfig(), request.getLinkid(), meResponse);
        }
    }

    private TioMessage getTioMessage(NotifyMsgSendRequest request) {
        TioMessage message = new TioMessage(request.getMsgType(), request.getMsgContent());
        message.setEventType(request.getEventType());
        message.setChatId(request.getLinkid());
        message.setSendUserId(request.getSendUserId());
        message.setToUserId(request.getToUserId());
        message.setSendTime(request.getSendTime());
        return message;
    }

    @Override
    public boolean updateIsRead(String linkid, Integer userId) {
        LambdaUpdateWrapper<NotifyMsgUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(NotifyMsgUserRel::getLinkid, linkid)
                .eq(NotifyMsgUserRel::getUserId, userId)
                .eq(NotifyMsgUserRel::getIsRead, false)
                .set(NotifyMsgUserRel::getIsRead, true);
        return notifyMsgUserRelService.update(updateWrapper);
    }

    @Override
    public boolean updateIsRead(Integer id) {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        return updateIsRead(chatUserId, id);
    }

    @Override
    public boolean updateIsRead(Integer userId, Integer id) {
        LambdaUpdateWrapper<NotifyMsgUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(NotifyMsgUserRel::getId, id)
                .eq(NotifyMsgUserRel::getUserId, userId)
                .eq(NotifyMsgUserRel::getIsRead, false)
                .set(NotifyMsgUserRel::getIsRead, true);
        return notifyMsgUserRelService.update(updateWrapper);
    }

    @Override
    public boolean allRead() {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        LambdaUpdateWrapper<NotifyMsgUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(NotifyMsgUserRel::getUserId, chatUserId)
                .eq(NotifyMsgUserRel::getIsRead, false)
                .set(NotifyMsgUserRel::getIsRead, true);
        return notifyMsgUserRelService.update(updateWrapper);
    }

    @Override
    public List<NotifyMsgInfoDto> getOfflineNotifyMsgList(Integer userId) {
        return baseMapper.selectOfflineNotifyMsgList(userId);
    }

    @Override
    public boolean updateOfflineNotifyMsg(Integer userId) {
        LambdaUpdateWrapper<NotifyMsgUserRel> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(NotifyMsgUserRel::getUserId, userId)
                .eq(NotifyMsgUserRel::getIsOffline, true)
                .set(NotifyMsgUserRel::getIsOffline, false);
        return notifyMsgUserRelService.update(updateWrapper);
    }
}
