package com.dot.msg.chat.listener.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 群聊创建事件
 *
 * @author Dao-yang
 * @date: 2024-02-29 17:11:01
 */

@Data
public class ChatGroupCreateEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 6238525473174356147L;

    /**
     * 群组ID
     */
    private Integer groupId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 群成员昵称拼接串
     */
    private String nicknameStr;

    /**
     * 群成员ID集合
     */
    private List<Integer> groupMemberIds;

    public ChatGroupCreateEvent(Integer groupId, Integer userId, String nicknameStr, List<Integer> groupMemberIds) {
        this.groupId = groupId;
        this.userId = userId;
        this.nicknameStr = nicknameStr;
        this.groupMemberIds = groupMemberIds;
    }
}
