package com.dot.sys.user.model;

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
 * 后台管理员表实体
 * 
 * @author Dao-yang
 * @date: 2024-01-10 10:29:35
 */
@Data
@TableName("eb_system_admin")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SystemAdmin", description="后台管理员表")
public class SystemAdmin implements Serializable {

	@Serial
	private static final long serialVersionUID =  7171387317169792376L;

	/**
	 * 后台管理员表ID
	 */
	@Schema(description = "后台管理员表ID")
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 后台管理员账号
	 */
	@Schema(description = "后台管理员账号")
	private String account;

	/**
	 * 手机号码
	 */
	@Schema(description = "手机号码")
	private String phone;

	/**
	 * 后台管理员密码
	 */
	@Schema(description = "后台管理员密码")
	private String pwd;

	/**
	 * 后台管理员姓名
	 */
	@Schema(description = "后台管理员姓名")
	private String realName;

	/**
	 * 管理员头像
	 */
	@Schema(description = "管理员头像")
	private String avatar;

	/**
	 * 角色ID
	 */
	@Schema(description = "角色ID")
	private String roles;

	/**
	 * 企业id，总平台管理员的企业id是0
	 */
	@Schema(description = "企业id，总平台管理员的企业id是0")
	private Integer enterpriseId;

	/**
	 * 行业分类(0:可以查看所有行业数据,1:礼品行业;2:医疗行业;3:大企业采购行业)
	 */
	@Schema(description = "行业分类(0:可以查看所有行业数据,1:礼品行业;2:医疗行业;3:大企业采购行业)")
	private Integer industryType;

	/**
	 * 管理员类型(ENTERPRISE:服务商企业,SUPPLIER:供应商，PL_ADMIN:平台管理员)
	 */
	@Schema(description = "管理员类型(ENTERPRISE:服务商企业,SUPPLIER:供应商，PL_ADMIN:平台管理员)")
	private String type;

	/**
	 * 后台管理员最后一次登录ip
	 */
	@Schema(description = "后台管理员最后一次登录ip")
	private String lastIp;

	/**
	 * 登录次数
	 */
	@Schema(description = "登录次数")
	private Integer loginCount;

	/**
	 * 后台管理员级别
	 */
	@Schema(description = "后台管理员级别")
	private Boolean level;

	/**
	 * 后台管理员状态 1有效0无效
	 */
	@Schema(description = "后台管理员状态 1有效0无效")
	private Boolean status;

	@Schema(description = "isDel")
	private Boolean isDel;

	/**
	 * 是否接收短信
	 */
	@Schema(description = "是否接收短信")
	private Boolean isSms;

	/**
	 * 扩展字段
	 */
	@Schema(description = "扩展字段")
	private String extra;

	/**
	 * 后台管理员添加时间
	 */
	@Schema(description = "后台管理员添加时间")
	private String createTime;

	/**
	 * 后台管理员最后一次登录时间
	 */
	@Schema(description = "后台管理员最后一次登录时间")
	private String updateTime;

	/**
	 * 最后登录时间
	 */
	@Schema(description = "最后登录时间")
	private String lastTime;

}
