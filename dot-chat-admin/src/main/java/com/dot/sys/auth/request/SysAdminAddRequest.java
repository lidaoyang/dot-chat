package com.dot.sys.auth.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员添加请求对象
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SysAdminAddRequest", description = "管理员添加请求对象")
public class SysAdminAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 9090686573093972664L;

    /**
     * 账号
     */
    @Schema(description = "账号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "账号不能为空")
    private String account;

    /**
     * 名称
     */
    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "名称不能为空")
    private String name;

    /**
     * 密码
     */
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度在6-20位之间")
    private String pwd;

    /**
     * 角色ID(多个用逗号分隔)
     */
    @Schema(description = "角色ID(多个用逗号分隔)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色ID不能为空")
    private String roles;

    /**
     * 状态(1:正常,0:禁用)
     */
    @Schema(description = "状态(1:正常,0:禁用)", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"true", "false"})
    @NotNull(message = "状态不能为空")
    private Boolean status;

}
