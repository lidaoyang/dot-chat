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
 * 聊天室群组表实体
 * 
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Data
@TableName("chat_group")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatGroup", description="聊天室群组表")
public class ChatGroup implements Serializable {

	@Serial
	private static final long serialVersionUID =  5482754903346397432L;

	@Schema(description = "id")
	@TableId(value = "id", type = IdType.AUTO)
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
	 * 备注
	 */
	@Schema(description = "备注")
	private String remark;

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
	 * 解散时间
	 */
	@Schema(description = "解散时间")
	private String dissolveTime;

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
