package com.dot.sys.auth.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统角色搜索请求对象
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SysRoleSearchRequest", description = "系统角色搜索请求对象")
public class SysRoleSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 7103375797647840549L;

    /**
     * 关键词搜索(id,name)
     */
    @Schema(description = "关键词搜索(id,name)")
    private String keywords;

    /**
     * 状态(1:显示,0:隐藏)
     */
    @Schema(description = "状态(1:显示,0:隐藏)",  allowableValues = {"true", "false"})
    private Boolean status;

    /**
     * 角色类型(0:超级管理员,1:普通管理员,2:演示管理员)
     */
    @Schema(description = "角色类型(0:超级管理员,1:普通管理员,2:演示管理员)", allowableValues = {"0", "1", "2"})
    @Range(min = 0, max = 1, message = "角色类型只能为0或1")
    private Integer type;
}
