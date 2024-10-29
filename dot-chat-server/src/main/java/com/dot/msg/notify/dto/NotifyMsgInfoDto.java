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
@Schema(name = "NotifyMsgInfoDto", description="通知消息")
public class NotifyMsgInfoDto implements Serializable {

	@Serial
	private static final long serialVersionUID =  4993582823092430571L;

	/**
	 * ID
	 */
	@Schema(description = "ID")
	private Integer id;

	/**
	 * 关联ID(好友申请关联申请ID/业务ID)
	 */
	@Schema(description = "关联ID(好友申请关联申请ID/业务ID)")
	private Integer linkid;

	/**
	 * 通知类型(1:个人通知,2:全局广播通知)
	 */
	@Schema(description = "通知类型(1:个人通知,2:全局广播通知)")
	private Integer notifyType;

	/**
	 * 消息类型(SYSTEM:系统消息;EVENT:事件消息;WARNING:预警消息)
	 */
	@Schema(description = "消息类型(SYSTEM:系统消息;EVENT:事件消息;WARNING:预警消息)")
	private String msgType;

	/**
	 * 事件类型(消息类型为事件消息时有值)
	 */
	@Schema(description = "事件类型(消息类型为事件消息时有值)")
	private String eventType;

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
	 * 消息内容
	 */
	@Schema(description = "消息内容")
	private String msgContent;

	/**
	 * 发送时间
	 */
	@Schema(description = "发送时间")
	private String sendTime;

}
