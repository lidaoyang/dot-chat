package com.dot.msg.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室群组详情返回信息
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatGroupInfoDto", description="聊天室群组详情返回信息")
public class ChatGroupInfoDto implements Serializable {

	@Serial
	private static final long serialVersionUID =  2623094202711527509L;

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
	 * 邀请进群是否需要群主或管理员确认(true:需要,false:不需要)
	 */
	@Schema(description = "邀请进群是否需要群主或管理员确认(true:需要,false:不需要)")
	private Boolean inviteCfm;

	/**
	 * 群主用户ID
	 */
	@Schema(description = "群主用户ID")
	private Integer groupLeaderId;

	/**
	 * 群管理员用户ID,多个用逗号分割,最多3个
	 */
	@Schema(description = "群管理员用户ID,多个用逗号分割,最多3个")
	private String managers;

	/**
	 * 群公告
	 */
	@Schema(description = "群公告")
	private String notice;

	/**
	 * 是否解散
	 */
	@Schema(description = "是否解散")
	private Boolean isDissolve;

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

}
