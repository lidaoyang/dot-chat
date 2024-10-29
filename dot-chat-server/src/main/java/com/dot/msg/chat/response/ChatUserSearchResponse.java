package com.dot.msg.chat.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室用户表-加好友搜索返回(不包含好友)
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatUserSearchResponse", description="聊天室用户表-加好友搜索返回(不包含好友)")
public class ChatUserSearchResponse implements Serializable {

	@Serial
	private static final long serialVersionUID =  9191778674935372365L;

	@Schema(description = "id")
	private Integer id;

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
	 * 企业名称
	 */
	@Schema(description = "企业名称")
	private String enterpriseName;

}
