package com.dot.sys.user.request;

import com.dot.comm.em.UserTypeEm;
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

    @Schema(description = "用户类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户类型不能为空")
    private UserTypeEm userType;

    @Schema(description = "企业id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "企业id不能为空")
    private Integer enterpriseId;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18888888")
    @NotBlank(message = "手机号不能为空")
    private String account;

    @Schema(description = "密码,密码登录时必传", example = "1~[6,18]")
    @NotNull(message = "密码不能为空")
    private String password;

    // @Schema(description = "登录方式,1-手机号登录,2-验证码登录", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    // @NotNull(message = "登录方式不能为空")
    // private Integer loginType;

    // @Schema(description = "验证码,验证码登录时必传")
    // private String captcha;
}
