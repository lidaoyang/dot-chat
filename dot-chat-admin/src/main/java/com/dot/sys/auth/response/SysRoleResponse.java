package com.dot.sys.auth.response;

import com.dot.sys.auth.em.RoleTypeEm;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统角色返回对象
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SysRoleResponse", description = "系统角色返回对象")
public class SysRoleResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 7103375797647840549L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Integer id;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String name;

    /**
     * 状态(1:显示,0:隐藏)
     */
    @Schema(description = "状态(1:显示,0:隐藏)")
    private Boolean status;

    /**
     * 角色类型(0:超级管理员,1:普通管理员,2:演示管理员)
     */
    @Schema(description = "角色类型(0:超级管理员,1:普通管理员,2:演示管理员)")
    private Integer type;

    /**
     * 角色类型(0:超级管理员,1:普通管理员,2:演示管理员)
     */
    @Schema(description = "角色类型(0:超级管理员,1:普通管理员,2:演示管理员)")
    private String typeDesc;

    public String getTypeDesc() {
        return RoleTypeEm.getDescByCode(this.type);
    }

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;

}
