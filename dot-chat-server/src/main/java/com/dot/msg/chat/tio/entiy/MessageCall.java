package com.dot.msg.chat.tio.entiy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.dot.msg.chat.tio.em.CallTypeEm;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 通话消息对象(语音通话/视频通话)
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/18 10:40
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageCall implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 通话消息类型枚举
     */
    private CallTypeEm callType;

    /**
     * 消息ID
     */
    private Integer msgId;

    /**
     * 候选者对象
     */
    private Map<String, Object> candidate;

    /**
     * 创建offer或answer产生session描述对象,(包含字段 type,sdp)
     */
    private Map<String, Object> desc;

    /**
     * 接受通话时间
     */
    private String acceptTime;

    /**
     * 挂断通话时间
     */
    private String hangupTime;

    /**
     * 通话时长(秒)
     */
    private int duration;

    /**
     * 通话中断方用户ID
     */
    private Integer droppedUserId;

}
