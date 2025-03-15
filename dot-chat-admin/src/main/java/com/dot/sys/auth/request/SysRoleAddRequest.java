package com.dot.sys.auth.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 系统角色添加请求对象
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SysRoleAddRequest", description = "系统角色添加请求对象")
public class SysRoleAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 7103375797647840549L;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色名称不能为空")
    private String name;

    /**
     * 状态(1:显示,0:隐藏)
     */
    @Schema(description = "状态(1:显示,0:隐藏)", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"true", "false"})
    @NotNull(message = "状态不能为空")
    private Boolean status;

    /**
     * 角色类型(0:超级管理员,1:普通管理员,2:演示管理员)
     */
    @Schema(description = "角色类型(0:超级管理员,1:普通管理员,2:演示管理员)", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"0", "1", "2"})
    @NotNull(message = "角色类型不能为空")
    @Range(min = 0, max = 2, message = "角色类型只能为0或1")
    private Integer type;

}
