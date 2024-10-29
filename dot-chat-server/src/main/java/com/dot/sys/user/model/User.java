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
import java.math.BigDecimal;

/**
 * 用户表实体
 * 
 * @author Dao-yang
 * @date: 2024-01-10 10:29:35
 */
@Data
@TableName("eb_user")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "User", description="用户表")
public class User implements Serializable {

	@Serial
	private static final long serialVersionUID =  7365551984701951535L;

	/**
	 * 用户id
	 */
	@Schema(description = "用户id")
	@TableId(value = "uid", type = IdType.AUTO)
	private Integer uid;

	/**
	 * 用户账号
	 */
	@Schema(description = "用户账号")
	private String account;

	/**
	 * 手机号码
	 */
	@Schema(description = "手机号码")
	private String phone;

	/**
	 * 用户密码
	 */
	@Schema(description = "用户密码")
	private String pwd;

	/**
	 * 真实姓名
	 */
	@Schema(description = "真实姓名")
	private String realName;

	/**
	 * 用户邮箱地址
	 */
	@Schema(description = "用户邮箱地址")
	private String email;

	/**
	 * 生日
	 */
	@Schema(description = "生日")
	private String birthday;

	/**
	 * 身份证号码
	 */
	@Schema(description = "身份证号码")
	private String cardId;

	/**
	 * 用户备注（管理员填写）
	 */
	@Schema(description = "用户备注（管理员填写）")
	private String mark;

	/**
	 * 合伙人id
	 */
	@Schema(description = "合伙人id")
	private Integer partnerId;

	/**
	 * 用户分组id
	 */
	@Schema(description = "用户分组id")
	private String groupId;

	/**
	 * 标签id
	 */
	@Schema(description = "标签id")
	private String tagId;

	/**
	 * 用户昵称
	 */
	@Schema(description = "用户昵称")
	private String nickname;

	/**
	 * 用户头像
	 */
	@Schema(description = "用户头像")
	private String avatar;

	/**
	 * 性别，0未知，1男，2女，3保密
	 */
	@Schema(description = "性别，0未知，1男，2女，3保密")
	private Integer sex;

	/**
	 * 添加ip
	 */
	@Schema(description = "添加ip")
	private String addIp;

	/**
	 * 最后一次登录ip
	 */
	@Schema(description = "最后一次登录ip")
	private String lastIp;

	/**
	 * 用户余额
	 */
	@Schema(description = "用户余额")
	private BigDecimal nowMoney;

	/**
	 * 佣金金额
	 */
	@Schema(description = "佣金金额")
	private BigDecimal brokeragePrice;

	/**
	 * 用户剩余积分
	 */
	@Schema(description = "用户剩余积分")
	private Integer integral;

	/**
	 * 用户剩余经验
	 */
	@Schema(description = "用户剩余经验")
	private Integer experience;

	/**
	 * 连续签到天数
	 */
	@Schema(description = "连续签到天数")
	private Integer signNum;

	/**
	 * 1为正常（解封），0为禁止（冻结）
	 */
	@Schema(description = "1为正常（解封），0为禁止（冻结）")
	private Boolean status;

	/**
	 * 等级
	 */
	@Schema(description = "等级")
	private Boolean level;

	/**
	 * 推广员id
	 */
	@Schema(description = "推广员id")
	private Integer spreadUid;

	/**
	 * 推广员关联时间
	 */
	@Schema(description = "推广员关联时间")
	private String spreadTime;

	/**
	 * 用户类型	0:  routine    游客 默认字符串
	 * 1:  enterprise_employees
	 * 企业员工
	 * 2:  enterprise_purchaser
	 * 企业采购员
	 */
	@Schema(description = "用户类型0:游客;1: 企业员工;2:企业采购员")
	private String userType;

	/**
	 * 是否为推广员
	 */
	@Schema(description = "是否为推广员")
	private Boolean isPromoter;

	/**
	 * 用户购买次数
	 */
	@Schema(description = "用户购买次数")
	private Integer payCount;

	/**
	 * 下级人数
	 */
	@Schema(description = "下级人数")
	private Integer spreadCount;

	/**
	 * 详细地址
	 */
	@Schema(description = "详细地址")
	private String addres;

	/**
	 * 管理员编号 
	 */
	@Schema(description = "管理员编号 ")
	private Integer adminid;

	/**
	 * 用户登陆类型，h5,wechat,routine
	 */
	@Schema(description = "用户登陆类型，h5,wechat,routine")
	private String loginType;

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

	/**
	 * 最后一次登录时间
	 */
	@Schema(description = "最后一次登录时间")
	private String lastLoginTime;

	/**
	 * 清除时间
	 */
	@Schema(description = "清除时间")
	private String cleanTime;

	/**
	 * 推广等级记录
	 */
	@Schema(description = "推广等级记录")
	private String path;

	/**
	 * 是否关注公众号
	 */
	@Schema(description = "是否关注公众号")
	private Boolean subscribe;

	/**
	 * 关注公众号时间
	 */
	@Schema(description = "关注公众号时间")
	private String subscribeTime;

	/**
	 * 国家，中国CN，其他OTHER
	 */
	@Schema(description = "国家，中国CN，其他OTHER")
	private String country;

	/**
	 * 扩展字段
	 */
	@Schema(description = "扩展字段")
	private String extra;

	/**
	 * 企业id
	 */
	@Schema(description = "企业id")
	private Integer enterpriseId;

	/**
	 * 行业分类(1:礼品行业;2:医疗行业,3.大企业采购行业)
	 */
	@Schema(description = "行业分类(1:礼品行业;2:医疗行业,3.大企业采购行业)")
	private Integer industryType;

	/**
	 * 首次下单购买时间
	 */
	@Schema(description = "首次下单购买时间")
	private String firstpayTime;

}
