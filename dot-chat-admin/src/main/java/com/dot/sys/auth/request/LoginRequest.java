package com.dot.sys.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * 手机密码登录请求对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "LoginRequest对象", description = "手机密码登录请求对象")
public class LoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18888888888")
    @NotBlank(message = "手机号不能为空")
    private String account;

    @Schema(description = "密码", example = "1~[6,18]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 18, message = "密码长度在6-18位之间")
    private String password;
}
