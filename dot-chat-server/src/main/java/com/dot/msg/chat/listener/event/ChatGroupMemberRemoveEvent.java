package com.dot.msg.chat.listener.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 管理员移除群成员事件
 *
 * @author Dao-yang
 * @date: 2024-02-29 17:11:01
 */

@Data
public class ChatGroupMemberRemoveEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 6238525473174356147L;

    /**
     * 群组ID
     */
    private Integer groupId;

    /**
     * 用户ID
     */
    private List<Integer> userIds;

    /**
     * 群成员昵称
     */
    private String nicknameStr;


    public ChatGroupMemberRemoveEvent(Integer groupId, List<Integer> userIds, String nicknameStr) {
        this.groupId = groupId;
        this.userIds = userIds;
        this.nicknameStr = nicknameStr;
    }
}
