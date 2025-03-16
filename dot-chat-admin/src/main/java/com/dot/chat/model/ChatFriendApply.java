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
 * 聊天室新好友申请表实体
 * 
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Data
@TableName("chat_friend_apply")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatFriendApply", description="聊天室新好友申请表")
public class ChatFriendApply implements Serializable {

	@Serial
	private static final long serialVersionUID =  3759540839303536249L;

	@Schema(description = "id")
	@TableId(value = "id", type = IdType.AUTO)
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
	 * 申请状态(0:申请中;1:同意;)
	 */
	@Schema(description = "申请状态(0:申请中;1:同意;)")
	private Boolean status;

	/**
	 * 来源
	 */
	@Schema(description = "来源")
	private String source;

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
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	private String createTime;

	/**
	 * 更新时间
	 */
	@Schema(description = "更新时间")
	private String updateTime;

}
