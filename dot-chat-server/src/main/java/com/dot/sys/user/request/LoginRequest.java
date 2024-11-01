package com.dot.sys.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

/**
 * 移动端手机密码登录请求对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "LoginRequest对象", description = "移动端手机密码登录请求对象")
public class LoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "188888880000")
    @NotBlank(message = "手机号不能为空")
    private String account;

    @Schema(description = "密码", example = "1~[6,18]",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "密码不能为空")
    private String password;
}
