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
	 * 用户电话
	 */
	@Schema(description = "用户电话")
	private String phone;

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
	 * 关联用户id(前端用户表/后端管理员表)
	 */
	@Schema(description = "关联用户id(前端用户表/后端管理员表)")
	private Integer userId;

	/**
	 * 用户类型(ENTERPRISE:服务商企业;SUPPLIER:供应商;PL_ADMIN:平台管理员;ENT_USER:企业用户)
	 */
	@Schema(description = "用户类型(ENTERPRISE:服务商企业;SUPPLIER:供应商;PL_ADMIN:平台管理员;ENT_USER:企业用户)")
	private String userType;

	/**
	 * 是否在线(true:在线;false:离线)
	 */
	@Schema(description = "是否在线(true:在线;false:离线)")
	private Integer enterpriseId;

	/**
	 * 企业名称
	 */
	@Schema(description = "企业名称")
	private String enterpriseName;

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
