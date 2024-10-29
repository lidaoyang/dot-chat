package com.dot.msg.chat.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室用户好友对象(精简)
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatUserFriendResponse", description="聊天室用户好友对象(精简)")
public class ChatUserFriendResponse implements Serializable {

	@Serial
	private static final long serialVersionUID =  9191778674935372365L;

	@Schema(description = "好友ID")
	private Integer friendId;

	/**
	 * 用户昵称
	 */
	@Schema(description = "用户昵称")
	private String nickname;

	/**
	 * 头像
	 */
	@Schema(description = "头像")
	private String avatar;

	/**
	 * 性别(0:保密,1:男,2:女)
	 */
	@Schema(description = "性别(0:保密,1:男,2:女)")
	private Integer sex;

	/**
	 * 好友备注
	 */
	@Schema(description = "好友备注")
	private String remark;

	/**
	 * 是否置顶
	 */
	@Schema(description = "是否置顶")
	private Boolean isTop;

	/**
	 * 昵称或备注首字母
	 */
	@Schema(description = "昵称或备注首字母")
	private String initial;

}
