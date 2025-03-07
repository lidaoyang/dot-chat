package com.dot.sys.auth.model;

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
 * 管理员表实体
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Data
@TableName("sys_admin")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SysAdmin", description = "管理员表")
public class SysAdmin implements Serializable {

    @Serial
    private static final long serialVersionUID = 9090686573093972664L;

    /**
     * ID
     */
    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 账号
     */
    @Schema(description = "账号")
    private String account;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;

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
     * 角色ID(多个用逗号分隔)
     */
    @Schema(description = "角色ID(多个用逗号分隔)")
    private String roles;

    /**
     * 状态(1:正常,0:禁用)
     */
    @Schema(description = "状态(1:正常,0:禁用)")
    private Boolean status;

    /**
     * 最后登录IP
     */
    @Schema(description = "最后登录IP")
    private String lastIp;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    private String lastTime;

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
