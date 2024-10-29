package com.dot.msg.chat.listener;

import com.dot.msg.chat.listener.event.ChatMsgSendEvent;
import com.dot.msg.chat.tio.service.ChatMsgSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Resource;

/**
 * 群聊事件监听
 *
 * @author: Dao-yang.
 * @date: Created in 2024/3/19 16:57
 */
@Slf4j
@Component
public class ChatMsgSendListener {

    @Resource
    private ChatMsgSendService chatMsgSendService;

    @TransactionalEventListener(ChatMsgSendEvent.class)
    public void onMsgSend(ChatMsgSendEvent event) {
        log.info("发送 {} 消息", event.getType());
        event.getMessageList().forEach(message -> {
            chatMsgSendService.sendAndSaveMsg(event.getChannelContext(), message);
        });
    }
}
