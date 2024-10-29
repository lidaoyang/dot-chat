package com.dot.msg.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户聊天室信息详情
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatRoomMsgInfoDto", description="用户聊天室信息详情")
public class ChatRoomMsgInfoDto implements Serializable {

	@Serial
	private static final long serialVersionUID =  7934754433847447124L;

	/**
	 * 聊天室id
	 */
	@Schema(description = "聊天室id")
	private String chatId;

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
	 * 是否置顶(true:置顶;false:不置顶)
	 */
	@Schema(description = "是否置顶(true:置顶;false:不置顶)")
	private Boolean isTop;

	/**
	 * 消息免打扰(true:开启免打扰;false:关闭免打扰)
	 */
	@Schema(description = "消息免打扰(true:开启免打扰;false:关闭免打扰)")
	private Boolean msgNoDisturb;

}
