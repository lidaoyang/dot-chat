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
 * 角色明细实体
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Data
@TableName("sys_role_detail")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SysRoleDetail", description = "角色明细")
public class SysRoleDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 6431094200901639917L;

    /**
     * ID
     */
    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    private Integer roleId;

    /**
     * 菜单ID
     */
    @Schema(description = "菜单ID")
    private Integer menuId;

    /**
     * 菜单URL地址
     */
    @Schema(description = "菜单URL地址")
    private String menuUrl;

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
