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
 * 聊天室用户表(关联管理员表和企业用户表)实体
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@TableName("chat_user")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatUser", description="聊天室用户表(关联管理员表和企业用户表)")
public class ChatUser implements Serializable {

	@Serial
	private static final long serialVersionUID =  9191778674935372365L;

	@Schema(description = "id")
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 用户昵称
	 */
	@Schema(description = "用户昵称")
	private String nickname;

	/**
	 * 用户电话(登录唯一账号)
	 */
	@Schema(description = "用户电话(登录唯一账号)")
	private String phone;

	/**
	 * 登录密码
	 */
	@Schema(description = "登录密码")
	private String pwd;

	/**
	 * 头像
	 */
	@Schema(description = "头像")
	private String avatar;

	/**
	 * 个人二维码
	 */
	@Schema(description = "个人二维码")
	private String qrcode;

	/**
	 * 性别(0:保密,1:男,2:女)
	 */
	@Schema(description = "性别(0:保密,1:男,2:女)")
	private Integer sex;

	/**
	 * 用户状态(1:正常,0:禁用)
	 */
	@Schema(description = "用户状态(1:正常,0:禁用)")
	private Boolean status;

	/**
	 * 删除标志
	 */
	@Schema(description = "删除标志")
	private Boolean delFlag;

	/**
	 * 是否在线
	 */
	@Schema(description = "是否在线")
	private Boolean isOnline;

	/**
	 * 个性签名
	 */
	@Schema(description = "个性签名")
	private String signature;

	/**
	 * 最后登录时间
	 */
	@Schema(description = "最后登录时间")
	private String lastLoginTime;

	/**
	 * 最后登录IP
	 */
	@Schema(description = "最后登录IP")
	private String lastIp;

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
