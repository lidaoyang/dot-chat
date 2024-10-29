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
 * 供应商表实体
 * 
 * @author Dao-yang
 * @date: 2024-01-10 10:29:35
 */
@Data
@TableName("pm_supplier")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "Supplier", description="供应商表")
public class Supplier implements Serializable {

	@Serial
	private static final long serialVersionUID =  4545187930616170732L;

	/**
	 * 供应商id
	 */
	@Schema(description = "供应商id")
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 账号
	 */
	@Schema(description = "账号")
	private String account;

	/**
	 * 供应商名称
	 */
	@Schema(description = "供应商名称")
	private String name;

	/**
	 * 联系人
	 */
	@Schema(description = "联系人")
	private String contacts;

	/**
	 * 联系人电话
	 */
	@Schema(description = "联系人电话")
	private String phone;

	/**
	 * 地址
	 */
	@Schema(description = "地址")
	private String address;

	/**
	 * 状态(0:删除 1：未开启 2:开启)
	 */
	@Schema(description = "状态(0:删除 1：未开启 2:开启)")
	private String status;

	/**
	 * 供应商类型
	 */
	@Schema(description = "供应商类型")
	private String type;

	/**
	 * 行业分类(1:礼品行业;2:医疗行业)
	 */
	@Schema(description = "行业分类(1:礼品行业;2:医疗行业)")
	private Integer industryType;

	/**
	 * 备注
	 */
	@Schema(description = "备注")
	private String mark;

	/**
	 * 注册时间
	 */
	@Schema(description = "注册时间")
	private String createTime;

	/**
	 * 更新时间
	 */
	@Schema(description = "更新时间")
	private String updateTime;

	/**
	 * 商品种类数量
	 */
	@Schema(description = "商品种类数量")
	private Integer productTypeNum;

	/**
	 * 订单数
	 */
	@Schema(description = "订单数")
	private Integer orderNum;

	/**
	 * 总件数
	 */
	@Schema(description = "总件数")
	private Integer totalNum;

	/**
	 * 商品总销售额
	 */
	@Schema(description = "商品总销售额")
	private BigDecimal totalSales;

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
	 * 密码
	 */
	@Schema(description = "密码")
	private String pwd;

	/**
	 * 头像
	 */
	@Schema(description = "头像")
	private String avatar;

	/**
	 * 邀请码
	 */
	@Schema(description = "邀请码")
	private String inviteCode;

}
