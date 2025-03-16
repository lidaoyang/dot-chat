package com.dot.chat.model;

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
 * 用户聊天室表实体
 * 
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Data
@TableName("chat_room")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatRoom", description="用户聊天室表")
public class ChatRoom implements Serializable {

	@Serial
	private static final long serialVersionUID =  3548666185715971218L;

	/**
	 * 主键ID
	 */
	@Schema(description = "主键ID")
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

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
	 * 时间戳
	 */
	@Schema(description = "时间戳")
	private Integer timestamp;

	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	private String createTime;

}
