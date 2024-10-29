package com.dot.msg.chat.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.dot.comm.em.UserTypeEm;
import com.dot.comm.entity.ResultBean;
import com.dot.msg.chat.response.ChatUserInfoResponse;
import com.dot.msg.chat.response.ChatUserResponse;
import com.dot.msg.chat.response.ChatUserSearchResponse;
import com.dot.msg.chat.service.ChatUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 聊天用户管理
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/10 18:11
 */
@Validated
@RestController
@RequestMapping("api/msg/chat/user")
@Tag(name = "聊天用户管理")
public class ChatUserController {

    @Resource
    private ChatUserService chatUserService;

    /**
     * 获取当前聊天用户
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "获取当前聊天用户")
    @GetMapping(value = "/get")
    @Parameter(name = "userType", description = "用户类型", required = true)
    public ResultBean<ChatUserResponse> getCurrentChatUser(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType) {
        return ResultBean.success(chatUserService.getCurrentChatUser(userType));
    }

    /**
     * 搜索聊天用户 搜索添加好友时使用,不包含好友
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "搜索聊天用户", description = "搜索添加好友时使用,不包含好友")
    @GetMapping(value = "/getSearchList")
    @Parameters({
            @Parameter(name = "userType", description = "用户类型", required = true),
            @Parameter(name = "keyword", description = "关键词", required = true)
    })
    public ResultBean<List<ChatUserSearchResponse>> getSearchChatUserList(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType,
                                                                          @RequestParam @NotNull(message = "关键词不能为空") String keyword) {
        return ResultBean.success(chatUserService.getSearchChatUserList(userType, keyword));
    }

    /**
     * 获取用户详情
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "获取用户详情")
    @GetMapping(value = "/info")
    @Parameters({
            @Parameter(name = "userType", description = "用户类型", required = true),
            @Parameter(name = "userId", description = "用户ID", required = true)
    })
    public ResultBean<ChatUserInfoResponse> getChatUserFriendInfo(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType,
                                                                  @RequestParam @NotNull(message = "用户ID不能为空") Integer userId) {
        return ResultBean.success(chatUserService.getChatUserInfo(userType, userId));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "更新用户头像")
    @PostMapping("/updateAvatar")
    @Parameter(name = "userType", description = "用户类型", required = true)
    public ResultBean<String> updateAvatar(MultipartFile image,
                                          @RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType) {
        return ResultBean.success(chatUserService.updateAvatar(image, userType), "操作成功");
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "更新用户头像")
    @PostMapping("/updateNickname")
    @Parameters({
            @Parameter(name = "userType", description = "用户类型", required = true),
            @Parameter(name = "nickname", description = "用户昵称"),
            @Parameter(name = "sex", description = "性别(0:保密,1:男,2:女)")
    })
    public ResultBean<Boolean> updateNickname(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType,
                                          @RequestParam(required = false) String nickname,
                                          @RequestParam(required = false) Integer sex) {
        return ResultBean.result(chatUserService.updateNickname(userType, nickname, sex));
    }

}
