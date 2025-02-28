package com.dot.msg.chat.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.dot.comm.entity.ResultBean;
import com.dot.msg.chat.request.ChatMsgSearchRequest;
import com.dot.msg.chat.response.ChatUserMsgResponse;
import com.dot.msg.chat.service.ChatMsgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天记录管理
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/10 18:11
 */
@Validated
@RestController
@RequestMapping("api/msg/chat/msg")
@Tag(name = "聊天记录管理")
public class ChatMsgController {

    @Resource
    private ChatMsgService chatMsgService;

    /**
     * 获取当前聊天用户
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "聊天记录列表", description = "获取当前登录用户的聊天室列表")
    @GetMapping(value = "/list")
    public ResultBean<List<ChatUserMsgResponse>> getChatRoomUserList(@Validated ChatMsgSearchRequest request) {
        return ResultBean.success(chatMsgService.getUserMsgList(request));
    }

    /**
     * 获取最近通话聊天记录
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "获取最近通话聊天记录", description = "首次进入时页面时调用")
    @GetMapping(value = "/getLastCallMsg")
    public ResultBean<ChatUserMsgResponse> getLastCallMsg() {
        return ResultBean.success(chatMsgService.getLastCallMsg());
    }


    /**
     * 转发消息
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "转发消息")
    @PostMapping(value = "/relay")
    @Parameters({
            @Parameter(name = "msgIds", description = "消息ID集合", required = true),
            @Parameter(name = "chatIds", description = "聊天室ID集合,与转发用户ID字段二选一"),
            @Parameter(name = "toUserIds", description = "转发用户ID集合,与聊天室ID字段二选一")
    })
    public ResultBean<Boolean> relayMsg(@RequestParam("msgIds") @NotEmpty(message = "消息ID集合不能为空") List<Integer> msgIds,
                                        @RequestParam(name = "chatIds", required = false) List<String> chatIds,
                                        @RequestParam(name = "toUserIds", required = false) List<Integer> toUserIds) {
        return ResultBean.result(chatMsgService.relayMsg(msgIds, chatIds, toUserIds));
    }

    /**
     * 删除消息
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "删除消息")
    @PostMapping(value = "/delete")
    @Parameter(name = "msgId", description = "消息ID", required = true)
    public ResultBean<Boolean> deleteUserMsg(@RequestParam("msgId") @NotNull(message = "消息ID不能为空") Integer msgId) {
        return ResultBean.result(chatMsgService.deleteUserMsg(msgId));
    }

    /**
     * 撤回消息
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "撤回消息")
    @PostMapping(value = "/revoke")
    @Parameter(name = "msgId", description = "消息ID", required = true)
    public ResultBean<Boolean> revokeMsg(@RequestParam("msgId") @NotNull(message = "消息ID不能为空") Integer msgId) {
        return ResultBean.result(chatMsgService.revokeMsg(msgId));
    }

    /**
     * 更新文件消息上传状态
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "更新文件消息上传状态")
    @PostMapping(value = "/updateFileMsgStatus")
    @Parameter(name = "msgId", description = "消息ID", required = true)
    public ResultBean<Boolean> updateFileMsgStatus(@RequestParam("msgId") @NotNull(message = "消息ID不能为空") Integer msgId) {
        return ResultBean.result(chatMsgService.updateFileMsgStatus(msgId));
    }
}
