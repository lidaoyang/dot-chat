package com.dot.msg.chat.listener.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 入群申请事件
 *
 * @author Dao-yang
 * @date: 2024-02-29 17:11:01
 */

@Data
public class ChatGroupApplyEvent implements Serializable {

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
     * 申请ID
     */
    private Integer applyId;

    /**
     * 群成管理员ID集合
     */
    private List<Integer> managerList;

    public ChatGroupApplyEvent(Integer groupId, Integer userId, Integer applyId, List<Integer> managerList) {
        this.groupId = groupId;
        this.userId = userId;
        this.applyId = applyId;
        this.managerList = managerList;
    }
}
