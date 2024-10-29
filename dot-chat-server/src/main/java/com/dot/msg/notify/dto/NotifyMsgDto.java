package com.dot.msg.notify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通知消息实体
 *
 * @author Dao-yang
 * @date: 2024-01-30 14:44:18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "NotifyMsgDto", description = "通知消息")
public class NotifyMsgDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 4993582823092430571L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Integer id;

    /**
     * 通知信息ID
     */
    @Schema(description = "通知信息ID")
    private Integer msgid;

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
     * 消息内容
     */
    @Schema(description = "消息内容")
    private String msgContent;

    /**
     * 发送时间
     */
    @Schema(description = "发送时间")
    private String sendTime;

    /**
     * 是否已读
     */
    @Schema(description = "是否已读")
    private Boolean isRead;

}
