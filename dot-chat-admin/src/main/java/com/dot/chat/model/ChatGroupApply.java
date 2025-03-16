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
 * 聊天室邀请入群申请表实体
 * 
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Data
@TableName("chat_group_apply")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatGroupApply", description="聊天室邀请入群申请表")
public class ChatGroupApply implements Serializable {

	@Serial
	private static final long serialVersionUID =  4857968372916930043L;

	@Schema(description = "id")
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 群组ID
	 */
	@Schema(description = "群组ID")
	private Integer groupId;

	/**
	 * 申请人用户ID集合,用逗号分割
	 */
	@Schema(description = "申请人用户ID集合,用逗号分割")
	private String applyUserIds;

	/**
	 * 邀请人用户ID
	 */
	@Schema(description = "邀请人用户ID")
	private Integer inviteUserId;

	/**
	 * 通过人用户ID
	 */
	@Schema(description = "通过人用户ID")
	private Integer approveUserId;

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
	 * 邀请原因
	 */
	@Schema(description = "邀请原因")
	private String applyReason;

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
