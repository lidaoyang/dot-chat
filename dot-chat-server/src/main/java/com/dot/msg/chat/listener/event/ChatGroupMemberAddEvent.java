package com.dot.msg.chat.listener.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 管理员添加群成员事件
 *
 * @author Dao-yang
 * @date: 2024-02-29 17:11:01
 */

@Data
public class ChatGroupMemberAddEvent implements Serializable {

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
     * 邀请人昵称
     */
    private String nickname;

    /**
     * 群成员昵称拼接串
     */
    private String nicknameStr;

    /**
     * 群成员ID集合
     */
    private List<Integer> groupMemberIds;

    public ChatGroupMemberAddEvent(Integer groupId, Integer userId, String nickname, String nicknameStr, List<Integer> groupMemberIds) {
        this.groupId = groupId;
        this.userId = userId;
        this.nickname = nickname;
        this.nicknameStr = nicknameStr;
        this.groupMemberIds = groupMemberIds;
    }
}
