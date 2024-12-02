package com.dot.msg.chat.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室新好友申请请求对象
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatFriendApplyAddRequest", description="聊天室新好友申请请求对象")
public class ChatFriendApplyAddRequest implements Serializable {

	@Serial
	private static final long serialVersionUID =  260395165663474068L;

	/**
	 * 好友ID
	 */
	@Schema(description = "好友ID",requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "好友ID不能为空")
	private Integer friendId;

	/**
	 * 好友备注
	 */
	@Schema(description = "好友备注")
	private String remark;

	/**
	 * 标签(多个标签用英文逗号分割)
	 */
	@Schema(description = "标签(多个标签用英文逗号分割)")
	private String label;

	/**
	 * 来源
	 */
	@Schema(description = "来源",requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "来源不能为空")
	private String source;

	/**
	 * 申请理由
	 */
	@Schema(description = "申请理由",requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "申请理由不能为空")
	private String applyReason;

}
