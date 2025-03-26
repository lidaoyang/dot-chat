package com.dot.chat.dto;

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
 * 用户聊天室Dto
 * 
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatRoomDto", description="用户聊天室Dto")
public class ChatRoomDto implements Serializable {

	@Serial
	private static final long serialVersionUID =  3548666185715971218L;

	/**
	 * 主键ID
	 */
	@Schema(description = "主键ID")
	private Integer id;

	/**
	 * 聊天室id
	 */
	@Schema(description = "聊天室id")
	private String chatId;

	/**
	 * 群组ID,群聊时groupId有值
	 */
	@Schema(description = "群组ID,群聊时groupId有值")
	private Integer groupId;

	/**
	 * 最新消息发送时间
	 */
	@Schema(description = "最新消息发送时间")
	private String lastTime;

}
