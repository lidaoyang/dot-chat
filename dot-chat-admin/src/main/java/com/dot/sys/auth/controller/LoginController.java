package com.dot.sys.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dot.comm.entity.PageParam;
import com.dot.comm.entity.ResultBean;
import com.dot.sys.auth.request.LoginRequest;
import com.dot.sys.auth.request.SysAdminAddRequest;
import com.dot.sys.auth.request.SysAdminEditRequest;
import com.dot.sys.auth.request.SysAdminSearchRequest;
import com.dot.sys.auth.response.LoginResponse;
import com.dot.sys.auth.response.SysAdminResponse;
import com.dot.sys.auth.service.LoginService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统-管理员管理
 *
 * @author: Dao-yang.
 * @date: Created in 2022/12/22 17:25
 */

@Slf4j
@Validated
@RestController
@RequestMapping("api/sys/auth/admin")
@Tag(name = "权限管理 -- 登录管理")
public class LoginController {

    @Resource
    private LoginService loginService;

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "登录")
    @PostMapping(value = "/login")
    public ResultBean<LoginResponse> login(@RequestBody @Validated LoginRequest request) {
        return ResultBean.success(loginService.login(request));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "刷新token")
    @PutMapping(value = "/refreshToken")
    public ResultBean<LoginResponse> refreshToken() {
        return ResultBean.success(loginService.refreshToken());
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "退出登录")
    @GetMapping(value = "/logout")
    public ResultBean<Boolean> logout() {
        loginService.logout();
        return ResultBean.success();
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "修改密码")
    @PutMapping(value = "/modifyPwd")
    @Parameters({
            @Parameter(name = "oldPwd", description = "旧密码", required = true),
            @Parameter(name = "newPwd", description = "新密码,长度(6-18)", required = true)
    })
    public ResultBean<Boolean> modifyStatus(@RequestParam("oldPwd") @NotBlank(message = "旧密码不能为空") String oldPwd,
                                            @RequestParam("newPwd") @NotBlank(message = "新密码不能为空") @Length(min = 6, max = 18, message = "新密码长度(6-18)") String newPwd) {
        return ResultBean.result(loginService.updatePassword(oldPwd, newPwd));
    }

}
