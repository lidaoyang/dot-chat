package com.dot.msg.notify.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通知信息关联用户实体
 *
 * @author Dao-yang
 * @date: 2024-01-30 14:44:18
 */
@Data
@TableName("chat_notify_msg_user_rel")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "NotifyMsgUserRel", description = "通知信息关联用户")
public class NotifyMsgUserRel implements Serializable {

    @Serial
    private static final long serialVersionUID = 8334492281403617023L;

    /**
     * ID
     */
    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Integer userId;

    /**
     * 通知信息ID
     */
    @Schema(description = "通知信息ID")
    private Integer notifyMsgId;

    /**
     * 关联ID(好友申请关联申请ID/业务ID)
     */
    @Schema(description = "关联ID(好友申请关联申请ID/业务ID)")
    private String linkid;

    /**
     * 通知发送用户ID
     */
    @Schema(description = "通知发送用户ID")
    private Integer sendUserId;

    /**
     * 通知接收用户ID
     */
    @Schema(description = "通知接收用户ID")
    private Integer toUserId;

    /**
     * 是否是离线消息(广播没有离线消息)
     */
    @Schema(description = "是否是离线消息(广播没有离线消息)")
    private Boolean isOffline;

    /**
     * 是否已读
     */
    @Schema(description = "是否已读")
    private Boolean isRead;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;

}
