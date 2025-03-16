package com.dot.chat.response;

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
 * 聊天室群组表实体
 * 
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatGroupResponse", description="聊天室群组表")
public class ChatGroupResponse implements Serializable {

	@Serial
	private static final long serialVersionUID =  5482754903346397432L;

	@Schema(description = "id")
	private Integer id;

	/**
	 * 群名称
	 */
	@Schema(description = "群名称")
	private String name;

	/**
	 * 群头像
	 */
	@Schema(description = "群头像")
	private String avatar;

	/**
	 * 群成员数
	 */
	@Schema(description = "群成员数")
	private Integer memberCount;

	/**
	 * 是否解散
	 */
	@Schema(description = "是否解散")
	private Boolean isDissolve;

	/**
	 * 解散时间
	 */
	@Schema(description = "解散时间")
	private String dissolveTime;

}
