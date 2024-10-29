package com.dot.msg.chat.listener.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 群聊名称更新事件
 *
 * @author Dao-yang
 * @date: 2024-02-29 17:11:01
 */

@Data
public class ChatGroupNameUpdateEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 6238525473174356147L;

    /**
     * 群组ID
     */
    private Integer groupId;

    /**
     * 群组名称
     */
    private String groupName;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 群成员昵称
     */
    private String nickname;

    public ChatGroupNameUpdateEvent(Integer groupId, String groupName, Integer userId, String nickname) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.userId = userId;
        this.nickname = nickname;
    }
}
