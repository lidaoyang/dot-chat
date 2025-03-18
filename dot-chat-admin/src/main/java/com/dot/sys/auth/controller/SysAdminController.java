package com.dot.sys.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dot.comm.entity.PageParam;
import com.dot.comm.entity.ResultBean;
import com.dot.sys.auth.request.*;
import com.dot.sys.auth.response.SysAdminResponse;
import com.dot.sys.auth.service.SysAdminService;
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
@Tag(name = "权限管理 -- 管理员管理")
public class SysAdminController {

    @Resource
    private SysAdminService sysAdminService;

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "管理员列表", description = "系统管理员列表，用于总后台展示系统管理员列表")
    @GetMapping(value = "/list")
    public ResultBean<IPage<SysAdminResponse>> getList(@Validated SysAdminSearchRequest request, PageParam pageParam) {
        return ResultBean.success(sysAdminService.getList(request, pageParam));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "添加管理员")
    @PostMapping(value = "/add")
    public ResultBean<Boolean> add(@RequestBody @Validated SysAdminAddRequest request) {
        return ResultBean.result(sysAdminService.add(request));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "编辑管理员")
    @PutMapping(value = "/edit")
    public ResultBean<Boolean> edit(@RequestBody @Validated SysAdminEditRequest request) {
        return ResultBean.result(sysAdminService.edit(request));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "修改管理员状态")
    @PutMapping(value = "/modifyStatus")
    @Parameters({
            @Parameter(name = "id", description = "管理员ID", required = true),
            @Parameter(name = "status", description = "管理员状态", required = true)
    })
    public ResultBean<Boolean> modifyStatus(@RequestParam("id") @NotNull(message = "管理员ID不能为空") Integer id,
                                            @RequestParam("status") @NotNull(message = "管理员状态不能为空") Boolean status) {
        return ResultBean.result(sysAdminService.updateStatus(id, status));
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
        return ResultBean.result(sysAdminService.updatePassword(oldPwd, newPwd));
    }

}
