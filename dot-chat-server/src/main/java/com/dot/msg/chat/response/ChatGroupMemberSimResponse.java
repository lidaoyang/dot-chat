package com.dot.msg.chat.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室群成员精简DTO
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatGroupMemberSimDto", description="聊天室群成员精简DTO")
public class ChatGroupMemberSimResponse implements Serializable {

	@Serial
	private static final long serialVersionUID =  3667689834847666711L;

	@Schema(description = "id")
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
	 * 用户昵称
	 */
	@Schema(description = "用户昵称")
	private String nickname;

	/**
	 * 群昵称
	 */
	@Schema(description = "群昵称")
	private String groupNickname;

	/**
	 * 头像
	 */
	@Schema(description = "头像")
	private String avatar;

	/**
	 * 是否在线
	 */
	@Schema(description = "是否在线")
	private Boolean isOnline;

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
	 * 来源
	 */
	@Schema(description = "来源")
	private String sourceDesc;

}
