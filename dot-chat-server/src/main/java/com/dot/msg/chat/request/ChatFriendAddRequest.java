package com.dot.msg.chat.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室好友表实体
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatFriendAddRequest", description="聊天室好友表")
public class ChatFriendAddRequest implements Serializable {

	@Serial
	private static final long serialVersionUID =  5539115298435028331L;

	/**
	 * 申请用户ID
	 */
	@Schema(description = "申请用户ID")
	private Integer applyUserId;

	/**
	 * 申请好友备注
	 */
	@Schema(description = "申请好友备注")
	private String remark;

	/**
	 * 好友昵称
	 */
	@Schema(description = "好友昵称")
	private String nickname;

	/**
	 * 标签(多个标签用英文逗号分割)
	 */
	@Schema(description = "标签(多个标签用英文逗号分割)")
	private String label;

	/**
	 * 好友ID
	 */
	@Schema(description = "好友ID")
	private Integer friendId;

	/**
	 * 来源
	 */
	@Schema(description = "来源")
	private String source;

	@Schema(description = "同意好友申请对象")
	private ChatFriendApplyAgreeRequest applyAgreeRequest;

}
