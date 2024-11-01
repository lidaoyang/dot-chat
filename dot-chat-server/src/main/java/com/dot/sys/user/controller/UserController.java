package com.dot.sys.user.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.dot.comm.entity.ResultBean;
import com.dot.sys.user.request.LoginRequest;
import com.dot.sys.user.response.LoginResponse;
import com.dot.sys.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 用户管理
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/10 18:11
 */
@Validated
@RestController
@RequestMapping("api/sys/user")
@Tag(name = "用户管理")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 账号密码登录
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "账号密码登录")
    @PostMapping(value = "/login")
    public ResultBean<LoginResponse> login(@RequestBody @Validated LoginRequest loginRequest) {
        return ResultBean.success(userService.login(loginRequest));
    }

    /**
     * 刷新token
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "刷新token")
    @PostMapping(value = "/refreshToken")
    public ResultBean<LoginResponse> refreshToken() {
        return ResultBean.success(userService.refreshToken());
    }

    /**
     * 退出登录
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "退出登录")
    @PostMapping(value = "/logout")
    public ResultBean<Boolean> login() {
        return ResultBean.success();
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "更新密码")
    @PostMapping("/updatePassword")
    @Parameters({
            @Parameter(name = "oldPwd", description = "原密码", required = true),
            @Parameter(name = "newPwd", description = "新密码(长度为6-20)", required = true)
    })
    public ResultBean<Boolean> updatePassword(
                                              @RequestParam @NotBlank(message = "原密码不能为空") String oldPwd,
                                              @RequestParam @NotBlank(message = "新密码不能为空") @Length(min = 6, max = 20, message = "新密码长度为6-20位") String newPwd) {
        return ResultBean.result(userService.updatePassword(oldPwd, newPwd));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "注册并登录")
    @PostMapping("/registerAndLogin")
    @Parameters({
            @Parameter(name = "account", description = "手机号(长度11位)", required = true),
            @Parameter(name = "password", description = "密码(长度为6-20)", required = true),
            @Parameter(name = "nickname", description = "昵称(最大长度32位)")
    })
    public ResultBean<LoginResponse> register(
            @RequestParam @NotBlank(message = "手机号不能为空") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误") String account,
            @RequestParam @NotBlank(message = "密码不能为空") @Length(min = 6, max = 20, message = "密码长度为6-20位") String password,
            @RequestParam(required = false) @Length(max = 32, message = "昵称长度不能超过32位") String nickname) {
        return ResultBean.success(userService.registerAndLogin(account, password, nickname));
    }
}
