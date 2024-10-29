package com.dot.msg.chat.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.dot.comm.em.UserTypeEm;
import com.dot.comm.entity.ResultBean;
import com.dot.msg.chat.response.ChatRoomMsgInfoResponse;
import com.dot.msg.chat.response.ChatRoomUserResponse;
import com.dot.msg.chat.response.ChatUnreadCountResponse;
import com.dot.msg.chat.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 聊天室管理
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/10 18:11
 */
@Validated
@RestController
@RequestMapping("api/msg/chat/room")
@Tag(name = "聊天室管理")
public class ChatRoomController {

    @Resource
    private ChatRoomService chatRoomService;

    /**
     * 获取当前聊天用户
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "聊天室列表", description = "获取当前登录用户的聊天室列表")
    @GetMapping(value = "/list")
    @Parameter(name = "userType", description = "用户类型", required = true)
    public ResultBean<List<ChatRoomUserResponse>> getChatRoomUserList(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType) {
        return ResultBean.success(chatRoomService.getChatRoomUserList(userType));
    }

    /**
     * 刷新聊天室列表
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "刷新聊天室列表", description = "获取当前登录用户的聊天室列表")
    @GetMapping(value = "/refresh/list")
    @Parameter(name = "userType", description = "用户类型", required = true)
    public ResultBean<List<ChatRoomUserResponse>> getChatRoomUserRefreshList(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType) {
        return ResultBean.success(chatRoomService.getChatRoomUserRefreshList(userType));
    }

    /**
     * 获取聊天室未读数
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "获取聊天室未读数", description = "获取聊天室消息和好友申请未读数")
    @GetMapping(value = "/getUnreadCount")
    @Parameter(name = "userType", description = "用户类型", required = true)
    public ResultBean<ChatUnreadCountResponse> getChatUnreadCount(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType) {
        return ResultBean.success(chatRoomService.getChatUnreadCount(userType));
    }

    /**
     * 从好友列表去聊天室
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "去聊天室", description = "从好友列表去聊天室")
    @GetMapping(value = "/gotoSendMsg")
    @Parameters({
            @Parameter(name = "userType", description = "用户类型", required = true),
            @Parameter(name = "friendId", description = "好友ID", required = true)
    })
    public ResultBean<String> gotoSendMsg(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType,
                                          @RequestParam @NotNull(message = "好友ID不能为空") Integer friendId) {
        return ResultBean.success(chatRoomService.gotoSendMsg(userType, friendId), "成功");
    }

    /**
     * 获取聊天室信息详情
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "获取聊天室信息详情")
    @GetMapping(value = "/getChatRoomMsgInfo")
    @Parameters({
            @Parameter(name = "userType", description = "用户类型", required = true),
            @Parameter(name = "chatId", description = "聊天室ID", required = true)
    })
    public ResultBean<ChatRoomMsgInfoResponse> getChatRoomMsgInfo(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType,
                                                                  @RequestParam @NotBlank(message = "聊天室ID不能为空") String chatId) {
        return ResultBean.success(chatRoomService.getChatRoomMsgInfo(userType, chatId));
    }

    /**
     * 更新消息免打扰
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "更新消息免打扰")
    @PostMapping(value = "/updateMsgNoDisturb")
    @Parameters({
            @Parameter(name = "userType", description = "用户类型", required = true),
            @Parameter(name = "chatId", description = "聊天室ID", required = true),
            @Parameter(name = "flag", description = "消息免打扰", required = true)
    })
    public ResultBean<Boolean> updateMsgNoDisturb(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType,
                                                  @RequestParam @NotBlank(message = "聊天室ID不能为空") String chatId,
                                                  @RequestParam @NotNull(message = "消息免打扰不能为空") Boolean flag) {
        return ResultBean.success(chatRoomService.updateMsgNoDisturb(userType, chatId, flag));
    }

    /**
     * 是否置顶
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "是否置顶")
    @PostMapping(value = "/updateIsTop")
    @Parameters({
            @Parameter(name = "userType", description = "用户类型", required = true),
            @Parameter(name = "chatId", description = "聊天室ID", required = true),
            @Parameter(name = "flag", description = "是否置顶", required = true)
    })
    public ResultBean<Boolean> updateIsTop(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType,
                                           @RequestParam @NotBlank(message = "聊天室ID不能为空") String chatId,
                                           @RequestParam @NotNull(message = "是否置顶不能为空") Boolean flag) {
        return ResultBean.success(chatRoomService.updateIsTop(userType, chatId, flag));
    }

    /**
     * 清空聊天记录
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "清空聊天记录")
    @PostMapping(value = "/cleanMsgList")
    @Parameters({
            @Parameter(name = "userType", description = "用户类型", required = true),
            @Parameter(name = "chatId", description = "聊天室ID", required = true)
    })
    public ResultBean<Boolean> cleanMsgList(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType,
                                           @RequestParam @NotBlank(message = "聊天室ID不能为空") String chatId) {
        return ResultBean.success(chatRoomService.cleanMsgList(userType, chatId));
    }


    /**
     * 删除聊天室
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "删除聊天室")
    @PostMapping(value = "/delete")
    @Parameters({
            @Parameter(name = "userType", description = "用户类型", required = true),
            @Parameter(name = "chatId", description = "聊天室ID", required = true)
    })
    public ResultBean<Boolean> deleteChatRoom(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType,
                                            @RequestParam @NotBlank(message = "聊天室ID不能为空") String chatId) {
        return ResultBean.success(chatRoomService.deleteChatRoom(userType, chatId));
    }
}
