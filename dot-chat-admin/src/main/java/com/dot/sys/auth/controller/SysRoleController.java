package com.dot.sys.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dot.comm.entity.PageParam;
import com.dot.comm.entity.ResultBean;
import com.dot.sys.auth.request.*;
import com.dot.sys.auth.response.SysRoleInfoResponse;
import com.dot.sys.auth.response.SysRoleResponse;
import com.dot.sys.auth.response.SysRoleSimResponse;
import com.dot.sys.auth.service.SysRoleService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统-角色管理
 *
 * @author: Dao-yang.
 * @date: Created in 2022/12/22 17:25
 */

@Slf4j
@Validated
@RestController
@RequestMapping("api/sys/auth/role")
@Tag(name = "权限管理 -- 角色管理")
public class SysRoleController {

    @Resource
    private SysRoleService sysRoleService;

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "角色列表", description = "系统角色列表，用于总后台展示系统角色列表")
    @GetMapping(value = "/list")
    public ResultBean<IPage<SysRoleResponse>> getList(@Validated SysRoleSearchRequest request, PageParam pageParam) {
        return ResultBean.success(sysRoleService.getList(request, pageParam));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "角色列表(精简)", description = "精简系统角色列表，用于下拉框")
    @GetMapping(value = "/simlist")
    @Parameter(name = "keywords", description = "关键词搜索(id,name)")
    public ResultBean<List<SysRoleSimResponse>> getSimList(@RequestParam(name = "keywords", required = false)String keywords) {
        return ResultBean.success(sysRoleService.getSimList(keywords));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "角色详情", description = "根据角色ID查询角色详情")
    @GetMapping(value = "/info")
    @Parameter(name = "roleId", description = "角色ID", required = true)
    public ResultBean<SysRoleInfoResponse> getInfo(@RequestParam("roleId") @NotNull(message = "角色ID不能为空") Integer roleId) {
        return ResultBean.success(sysRoleService.getInfo(roleId));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "添加角色")
    @PostMapping(value = "/add")
    public ResultBean<Boolean> add(@RequestBody @Validated SysRoleAddRequest request) {
        return ResultBean.result(sysRoleService.add(request));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "编辑角色")
    @PutMapping(value = "/edit")
    public ResultBean<Boolean> edit(@RequestBody @Validated SysRoleEditRequest request) {
        return ResultBean.result(sysRoleService.edit(request));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "删除角色", description = "根据角色ID删除角色")
    @DeleteMapping(value = "/delete")
    @Parameter(name = "roleId", description = "角色ID", required = true)
    public ResultBean<Boolean> delete(@RequestParam @NotNull(message = "角色ID不能为空") Integer roleId) {
        return ResultBean.result(sysRoleService.delete(roleId));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "修改角色状态")
    @PutMapping(value = "/modifyStatus")
    @Parameters({
            @Parameter(name = "roleId", description = "角色ID", required = true),
            @Parameter(name = "status", description = "角色状态", required = true)
    })
    public ResultBean<Boolean> modifyStatus(@RequestParam @NotNull(message = "角色ID不能为空") Integer roleId,
                                            @RequestParam @NotNull(message = "角色状态不能为空") Boolean status) {
        return ResultBean.result(sysRoleService.updateStatus(roleId, status));
    }

}
