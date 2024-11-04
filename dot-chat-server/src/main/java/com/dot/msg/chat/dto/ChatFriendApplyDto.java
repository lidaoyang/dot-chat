package com.dot.msg.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室新好友申请表实体
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatFriendApplyDto", description="聊天室新好友申请")
public class ChatFriendApplyDto implements Serializable {

	@Serial
	private static final long serialVersionUID =  260395165663474068L;

	@Schema(description = "id")
	private Integer id;

	/**
	 * 申请用户ID
	 */
	@Schema(description = "申请用户ID")
	private Integer applyUserId;

	/**
	 * 好友ID
	 */
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
	 * 个性签名
	 */
	@Schema(description = "个性签名")
	private String signature;

	/**
	 * 未读数
	 */
	@Schema(description = "未读数")
	private Integer unreadCount;


	/**
	 * 申请状态(0:申请中;1:同意;)
	 */
	@Schema(description = "申请状态(0:申请中;1:同意;)")
	private Integer status;

	/**
	 * 申请理由
	 */
	@Schema(description = "申请理由")
	private String applyReason;

	/**
	 * 申请回复
	 */
	@Schema(description = "申请回复")
	private String applyReply;

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
	@Schema(description = "来源")
	private String source;
}
