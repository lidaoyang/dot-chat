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
 * 聊天室群成员表实体
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@TableName("chat_group_member")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatGroupMember", description="聊天室群成员表")
public class ChatGroupMember implements Serializable {

	@Serial
	private static final long serialVersionUID =  3667689834847666711L;

	@Schema(description = "id")
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 群组ID
	 */
	@Schema(description = "群组ID")
	private Integer groupId;

	/**
	 * 群成员用户ID
	 */
	@Schema(description = "群成员用户ID")
	private Integer userId;

	/**
	 * 群成员昵称
	 */
	@Schema(description = "群成员昵称")
	private String nickname;

	/**
	 * 是否是群主
	 */
	@Schema(description = "是否是群主")
	private Boolean isGroupLeader;

	/**
	 * 是否是群管理员
	 */
	@Schema(description = "是否是群管理员")
	private Boolean isGroupManager;

	/**
	 * 邀请人用户ID
	 */
	@Schema(description = "邀请人用户ID")
	private Integer inviteUserId;

	/**
	 * 来源
	 */
	@Schema(description = "来源")
	private String source;

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
