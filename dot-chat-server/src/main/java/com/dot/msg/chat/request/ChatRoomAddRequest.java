package com.dot.msg.chat.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 添加用户聊天室
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatRoomAddRequest", description="添加用户聊天室")
public class ChatRoomAddRequest implements Serializable {

	@Serial
	private static final long serialVersionUID =  7934754433847447124L;

	/**
	 * 聊天室类型(SINGLE:单聊,GROUP:群聊)
	 */
	@Schema(description = "聊天室类型(SINGLE:单聊,GROUP:群聊)")
	private String chatType;

	/**
	 * 群组ID,群聊时groupId有值
	 */
	@Schema(description = "群组ID,群聊时groupId有值")
	private Integer groupId;

	/**
	 * 发送用户id
	 */
	@Schema(description = "发送用户id")
	private Integer sendUserId;

	/**
	 * 消息的发送或接收人(群消息时为发送人)
	 */
	@Schema(description = "消息的发送或接收人(群消息时为发送人)")
	private Integer toUserId;

}
