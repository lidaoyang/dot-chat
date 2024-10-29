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
 * 企业信息表实体
 * 
 * @author Dao-yang
 * @date: 2024-01-10 10:29:35
 */
@Data
@TableName("pm_enterprise")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "Enterprise", description="企业信息表")
public class Enterprise implements Serializable {

	@Serial
	private static final long serialVersionUID =  2960882553340171874L;

	/**
	 * 企业id
	 */
	@Schema(description = "企业id")
	@TableId(value = "enterprise_id", type = IdType.AUTO)
	private Integer enterpriseId;

	/**
	 * 企业类型（toB,toC）
	 */
	@Schema(description = "企业类型（toB,toC）")
	private String enterpriseType;

	/**
	 * 企业账号
	 */
	@Schema(description = "企业账号")
	private String account;

	/**
	 * 企业名称
	 */
	@Schema(description = "企业名称")
	private String name;

	/**
	 * 企业简称
	 */
	@Schema(description = "企业简称")
	private String shorterName;

	/**
	 * 企业代表
	 */
	@Schema(description = "企业代表")
	private String realName;

	/**
	 * 电话
	 */
	@Schema(description = "电话")
	private String phone;

	/**
	 * 企业地址
	 */
	@Schema(description = "企业地址")
	private String address;

	/**
	 * 头像
	 */
	@Schema(description = "头像")
	private String avatar;

	/**
	 * 企业logo
	 */
	@Schema(description = "企业logo")
	private String logo;

	/**
	 * 企业移动端logo
	 */
	@Schema(description = "企业移动端logo")
	private String logoMobile;

	/**
	 * 头图片
	 */
	@Schema(description = "头图片")
	private String headPicture;

	/**
	 * 营业执照
	 */
	@Schema(description = "营业执照")
	private String businessLicense;

	/**
	 * 备注
	 */
	@Schema(description = "备注")
	private String mark;

	/**
	 * 排序
	 */
	@Schema(description = "排序")
	private Integer sort;

	/**
	 * 状态(0关闭,1开启)
	 */
	@Schema(description = "状态(0关闭,1开启)")
	private Boolean status;

	/**
	 * 是否删除
	 */
	@Schema(description = "是否删除")
	private Boolean isDel;

	/**
	 * 余额
	 */
	@Schema(description = "余额")
	private BigDecimal balance;

	/**
	 * 待结算金额
	 */
	@Schema(description = "待结算金额")
	private BigDecimal waitSettled;

	/**
	 * 不可提现余额
	 */
	@Schema(description = "不可提现余额")
	private BigDecimal noWithdrawal;

	/**
	 * 行业分类(1:礼品行业;2:医疗行业)
	 */
	@Schema(description = "行业分类(1:礼品行业;2:医疗行业)")
	private Boolean industryType;

	/**
	 * 企业简介
	 */
	@Schema(description = "企业简介")
	private String profile;

	/**
	 * 扩展字段
	 */
	@Schema(description = "扩展字段")
	private String extra;

	/**
	 * 企业税号
	 */
	@Schema(description = "企业税号")
	private String dutyParagraph;

	/**
	 * 企业域名地址域名
	 */
	@Schema(description = "企业域名地址域名")
	private String domainNameAddress;

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
