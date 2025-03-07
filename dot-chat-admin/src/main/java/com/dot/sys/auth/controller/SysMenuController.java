package com.dot.sys.auth.controller;

import com.dot.comm.entity.ResultBean;
import com.dot.sys.auth.request.SysMenuAddRequest;
import com.dot.sys.auth.request.SysMenuEditRequest;
import com.dot.sys.auth.request.SysMenuSearchRequest;
import com.dot.sys.auth.response.SysMenuResponse;
import com.dot.sys.auth.response.SysMenuSimResponse;
import com.dot.sys.auth.service.SysMenuService;
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
 * 系统-菜单管理
 *
 * @author: Dao-yang.
 * @date: Created in 2022/12/22 17:25
 */

@Slf4j
@Validated
@RestController
@RequestMapping("api/sys/auth/menu")
@Tag(name = "权限管理 -- 菜单管理")
public class SysMenuController {

    @Resource
    private SysMenuService sysMenuService;

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "系统菜单列表", description = "系统菜单列表，用于总后台展示系统菜单列表")
    @GetMapping(value = "/list")
    public ResultBean<List<SysMenuResponse>> getList(SysMenuSearchRequest request) {
        return ResultBean.success(sysMenuService.getList(request));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "系统菜单列表(精简)", description = "获取精简系统菜单列表，设置角色菜单权限时使用")
    @GetMapping(value = "/simlist")
    public ResultBean<List<SysMenuSimResponse>> getSimList(SysMenuSearchRequest request) {
        return ResultBean.success(sysMenuService.getSimList(request));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "添加系统菜单")
    @PostMapping(value = "/add")
    public ResultBean<Boolean> add(@RequestBody @Validated SysMenuAddRequest request) {
        return ResultBean.result(sysMenuService.add(request));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "编辑系统菜单")
    @PutMapping(value = "/edit")
    public ResultBean<Boolean> edit(@RequestBody @Validated SysMenuEditRequest request) {
        return ResultBean.result(sysMenuService.edit(request));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "删除系统菜单", description = "根据菜单ID删除菜单")
    @DeleteMapping(value = "/delete")
    @Parameter(name = "menuId", description = "菜单ID", required = true)
    public ResultBean<Boolean> delete(@RequestParam @NotNull(message = "菜单ID不能为空") Integer menuId) {
        return ResultBean.result(sysMenuService.delete(menuId));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "修改菜单状态")
    @PutMapping(value = "/modifyStatus")
    @Parameters({
            @Parameter(name = "menuId", description = "菜单ID", required = true),
            @Parameter(name = "status", description = "菜单状态", required = true),
            @Parameter(name = "updateChild", description = "是否修改子集(默认false)")
    })
    public ResultBean<Boolean> modifyStatus(@RequestParam @NotNull(message = "菜单ID不能为空") Integer menuId,
                                            @RequestParam @NotNull(message = "菜单状态不能为空") Boolean status,
                                            @RequestParam(required = false, defaultValue = "false") Boolean updateChild) {
        return ResultBean.result(sysMenuService.updateStatus(menuId, status, updateChild));
    }

}
