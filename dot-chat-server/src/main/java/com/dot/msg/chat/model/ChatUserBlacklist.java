package com.dot.msg.chat.model;

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
 * 聊天室用户黑名单表实体
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@TableName("chat_user_blacklist")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatUserBlacklist", description="聊天室用户黑名单表")
public class ChatUserBlacklist implements Serializable {

	@Serial
	private static final long serialVersionUID =  7898850670731898510L;

	@Schema(description = "id")
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 用户ID
	 */
	@Schema(description = "用户ID")
	private Integer userId;

	/**
	 * 拉黑用户ID
	 */
	@Schema(description = "拉黑用户ID")
	private Integer blackUserId;

	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	private String createTime;

}
