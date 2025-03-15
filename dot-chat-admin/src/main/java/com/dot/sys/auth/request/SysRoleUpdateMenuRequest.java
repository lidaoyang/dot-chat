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
 * 系统角色更加权限菜单请求对象
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SysRoleUpdateMenuRequest", description = "系统角色更加权限菜单请求对象")
public class SysRoleUpdateMenuRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 7103375797647840549L;

    /**
     * ID
     */
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "角色ID不能为空")
    private Integer id;

    /**
     * 菜单ID集合,多个用逗号分割
     */
    @Schema(description = "菜单ID集合,多个用逗号分割", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜单ID集合不能为空")
    private String menuIds;

}
