package com.dot.msg.chat.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室用户好友详情
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatUserFriendInfoResponse", description="聊天室用户好友详情")
public class ChatUserFriendInfoResponse implements Serializable {

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
	 * 企业名称
	 */
	@Schema(description = "企业名称")
	private String enterpriseName;

	/**
	 * 标签(多个标签用英文逗号分割)
	 */
	@Schema(description = "标签(多个标签用英文逗号分割)")
	private String label;

	/**
	 * 来源
	 */
	@Schema(description = "来源")
	private String sourceDesc;
}
