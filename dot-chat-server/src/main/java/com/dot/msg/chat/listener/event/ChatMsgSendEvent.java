package com.dot.msg.chat.listener.event;

import com.dot.msg.chat.tio.entiy.TioMessage;
import lombok.Data;
import org.tio.core.ChannelContext;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 消息发送事件
 *
 * @author Dao-yang
 * @date: 2024-02-29 17:11:01
 */

@Data
public class ChatMsgSendEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 6238525473174356147L;

    /**
     * 类型(转发,撤回)
     */
    private String type;

    /**
     * 上下文通道
     */
    private ChannelContext channelContext;
    /**
     * 聊天信息集合
     */
    private List<TioMessage> messageList;

    public ChatMsgSendEvent(ChannelContext channelContext, List<TioMessage> messageList, String type) {
        this.channelContext = channelContext;
        this.messageList = messageList;
        this.type = type;
    }
}
