package com.dot.msg.chat.listener;

import cn.hutool.core.util.ObjectUtil;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.exception.ApiException;
import com.dot.msg.chat.em.ChatSourceEm;
import com.dot.msg.chat.listener.event.UserRegisterAddDefaultFriendEvent;
import com.dot.msg.chat.request.ChatFriendApplyAddRequest;
import com.dot.msg.chat.request.ChatFriendApplyAgreeRequest;
import com.dot.msg.chat.service.ChatFriendApplyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;


/**
 * 群聊事件监听
 *
 * @author: Dao-yang.
 * @date: Created in 2024/3/19 16:57
 */
@Slf4j
@Component
public class UserRegisterAddDefaultFriendListener {

    @Resource
    private ChatFriendApplyService chatFriendApplyService;

    @EventListener(UserRegisterAddDefaultFriendEvent.class)
    public void onUserRegisterAfter(UserRegisterAddDefaultFriendEvent event) {
        log.info("用户 {} 添加默认好友 {}", event.getUserId(), event.getFriendUser().getNickname());
        ChatFriendApplyAddRequest applyAddRequest = new ChatFriendApplyAddRequest();
        applyAddRequest.setSource(ChatSourceEm.SYSTEM.name());
        applyAddRequest.setChatUserId(event.getUserId());
        applyAddRequest.setApplyReason("我是" + event.getFriendUser().getNickname());
        applyAddRequest.setFriendId(event.getFriendUser().getId());
        applyAddRequest.setRemark("");
        Integer applyId = chatFriendApplyService.addChatFriendApplyRetApplyId(applyAddRequest);
        if (ObjectUtil.isNull(applyId)) {
            log.error("用户 {} 添加默认好友{}失败", event.getUserId(), event.getFriendUser().getNickname());
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "添加好友失败");
        }

        ChatFriendApplyAgreeRequest agreeRequest = new ChatFriendApplyAgreeRequest();
        agreeRequest.setApplyId(applyId);
        agreeRequest.setNickname(event.getFriendUser().getNickname());
        String chatId = chatFriendApplyService.agreeFriendApplyRetChatId(agreeRequest, event.getFriendUser());
        if (ObjectUtil.isNull(chatId)) {
            log.error("用户 {} 添加默认好友{}失败,chatId:{}", event.getUserId(), event.getFriendUser().getNickname(), chatId);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "添加好友失败");
        }
    }
}
