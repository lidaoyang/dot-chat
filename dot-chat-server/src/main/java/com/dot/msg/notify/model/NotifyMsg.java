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
 * 通知消息实体
 * 
 * @author Dao-yang
 * @date: 2024-01-30 14:44:18
 */
@Data
@TableName("chat_notify_msg")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "NotifyMsg", description="通知消息")
public class NotifyMsg implements Serializable {

	@Serial
	private static final long serialVersionUID =  4993582823092430571L;

	/**
	 * ID
	 */
	@Schema(description = "ID")
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 关联ID(好友申请关联申请ID/业务ID)
	 */
	@Schema(description = "关联ID(好友申请关联申请ID/业务ID)")
	private String linkid;


	/**
	 * 通知用户ID(全局通知类型时为业务ID)
	 */
	@Schema(description = "通知用户ID(全局通知类型时为业务ID)")
	private Integer userId;

	/**
	 * 通知类型(1:个人通知,2:业务通知,3:广播)
	 */
	@Schema(description = "通知类型(1:个人通知,2:业务通知,3:广播)")
	private Integer notifyType;

	/**
	 * 消息类型(SYSTEM:系统消息;EVENT:事件消息;WARNING:预警消息)
	 */
	@Schema(description = "消息类型(SYSTEM:系统消息;EVENT:事件消息;WARNING:预警消息;NOTICE:通知消息)")
	private String msgType;

	/**
	 * 事件类型(消息类型为事件消息时有值)
	 */
	@Schema(description = "事件类型(消息类型为事件消息时有值)")
	private String eventType;

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
