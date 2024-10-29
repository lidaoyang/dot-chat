package com.dot.msg.notify.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.dot.msg.chat.tio.em.EventTypeEm;
import com.dot.msg.chat.tio.em.MsgTypeEm;
import com.dot.msg.notify.em.NotifyBizEm;
import com.dot.msg.notify.em.NotifyTypeEm;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发送通知消息请求对象
 * 
 * @author Dao-yang
 * @date: 2024-01-30 14:44:18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "NotifyMsgSendRequest", description="发送通知消息请求对象")
public class NotifyMsgSendRequest implements Serializable {

	@Serial
	private static final long serialVersionUID =  4993582823092430571L;


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
	 * 通知接收用户ID(全局通知类型时为业务ID)
	 */
	@Schema(description = "通知接收用户ID(通知类型为业务业务时,为业务ID)")
	private Integer toUserId;

	/**
	 * 通知类型(1:个人通知,2:业务通知,3:广播)
	 */
	@Schema(description = "通知类型(1:个人通知,2:业务通知,3:广播)")
	private NotifyTypeEm notifyType;

	/**
	 * 通知业务(通知类型为业务通知时有值)
	 */
	private NotifyBizEm notifyBiz;

	/**
	 * 消息类型(SYSTEM:系统消息;EVENT:事件消息;WARNING:预警消息)
	 */
	@Schema(description = "消息类型(SYSTEM:系统消息;EVENT:事件消息;WARNING:预警消息;NOTICE:通知消息)")
	private MsgTypeEm msgType;

	/**
	 * 事件类型(消息类型为事件消息时有值)
	 */
	@Schema(description = "事件类型(消息类型为事件消息时有值)")
	private EventTypeEm eventType;

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
