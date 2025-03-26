package com.dot.chat.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dot.chat.request.ChatUserSearchRequest;
import com.dot.chat.response.ChatUserRegisterCountResponse;
import com.dot.chat.response.ChatUserResponse;
import com.dot.chat.response.ChatUserSimResponse;
import com.dot.chat.service.ChatUserService;
import com.dot.comm.entity.PageParam;
import com.dot.comm.entity.ResultBean;
import com.dot.sys.auth.request.LoginRequest;
import com.dot.sys.auth.response.LoginResponse;
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

import java.util.List;

/**
 * IM聊天室 -- 用户管理
 *
 * @author: Dao-yang.
 * @date: Created in 2022/12/22 17:25
 */

@Slf4j
@Validated
@RestController
@RequestMapping("api/chat/user")
@Tag(name = "IM聊天室 -- 用户管理")
public class ChatUserController {

    @Resource
    private ChatUserService chatUserService;

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "用户列表")
    @GetMapping(value = "/list")
    public ResultBean<IPage<ChatUserResponse>> getList(@Validated ChatUserSearchRequest request, PageParam pageParam) {
        return ResultBean.success(chatUserService.getList(request, pageParam));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "用户列表-精简", description = "用户下拉框")
    @GetMapping(value = "/simlist")
    public ResultBean<List<ChatUserSimResponse>> getSimList(@Validated ChatUserSearchRequest request) {
        return ResultBean.success(chatUserService.getSimList(request));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "修改状态")
    @PutMapping(value = "/modifyStatus")
    @Parameters({
            @Parameter(name = "id", description = "用户ID", required = true),
            @Parameter(name = "status", description = "用户状态(true:正常,false:禁用)", required = true)
    })
    public ResultBean<Boolean> modifyStatus(@RequestParam("id") @NotNull(message = "用户ID不能为空") Integer id,
                                            @RequestParam("status") @NotNull(message = "状态不能为空") Boolean status) {
        return ResultBean.result(chatUserService.updateUserStatus(id, status));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "用户注册统计")
    @GetMapping(value = "/registerCount")
    public ResultBean<List<ChatUserRegisterCountResponse>> registerCount() {
        return ResultBean.success(chatUserService.getUserRegisterCount());
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "每日用户列表", description = "按日期获取用户列表,用户注册统计查询每日用户")
    @Parameter(name = "date", description = "注册日期", required = true)
    @GetMapping(value = "/listbydate")
    public ResultBean<List<ChatUserSimResponse>> getUserSimList(@RequestParam("date") @NotBlank(message = "注册日期不能为空") String date) {
        return ResultBean.success(chatUserService.getUserSimList(date));
    }

}
