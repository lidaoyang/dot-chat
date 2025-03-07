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
 * 聊天室邀请好友进群申请和用户关联表实体
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@TableName("chat_group_apply_user_rel")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatGroupApplyUserRel", description="聊天室邀请好友进群申请和用户关联表")
public class ChatGroupApplyUserRel implements Serializable {

	@Serial
	private static final long serialVersionUID =  6788892504330602120L;

	@Schema(description = "id")
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 用户ID
	 */
	@Schema(description = "用户ID")
	private Integer userId;

	/**
	 * 申请ID
	 */
	@Schema(description = "申请ID")
	private Integer applyId;

	/**
	 * 是否已读
	 */
	@Schema(description = "是否已读")
	private Boolean isRead;

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
