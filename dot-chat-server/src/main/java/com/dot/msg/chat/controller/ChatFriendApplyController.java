package com.dot.msg.chat.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.dot.comm.entity.ResultBean;
import com.dot.msg.chat.request.ChatFriendApplyAddRequest;
import com.dot.msg.chat.request.ChatFriendApplyAgreeRequest;
import com.dot.msg.chat.request.ChatFriendApplyReplayRequest;
import com.dot.msg.chat.response.ChatFriendApplyInfoResponse;
import com.dot.msg.chat.response.ChatFriendApplyResponse;
import com.dot.msg.chat.service.ChatFriendApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 聊天好友申请管理
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/10 18:11
 */
@Validated
@RestController
@RequestMapping("api/msg/chat/friend/apply")
@Tag(name = "聊天好友申请管理")
public class ChatFriendApplyController {

    @Resource
    private ChatFriendApplyService chatFriendApplyService;


    /**
     * 获取当前聊天用户
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "获取好友申请列表")
    @GetMapping(value = "/list")
    public ResultBean<List<ChatFriendApplyResponse>> getChatFriendApplyList() {
        return ResultBean.success(chatFriendApplyService.getChatFriendApplyList());
    }

    /**
     * 获取当前聊天用户
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "获取好友申请详情")
    @GetMapping(value = "/info")
    @Parameter(name = "applyId", description = "申请ID", required = true)
    public ResultBean<ChatFriendApplyInfoResponse> getChatFriendApplyInfo(@RequestParam @NotNull(message = "申请ID不能为空") Integer applyId) {
        return ResultBean.success(chatFriendApplyService.getChatFriendApplyInfo(applyId));
    }

    /**
     * 添加好友申请
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "添加好友申请")
    @PostMapping(value = "/add")
    public ResultBean<Boolean> addChatFriendApply(@RequestBody @Validated ChatFriendApplyAddRequest request) {
        return ResultBean.success(chatFriendApplyService.addChatFriendApply(request));
    }

    /**
     * 回复好友申请
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "回复好友申请")
    @PostMapping(value = "/replay")
    public ResultBean<Boolean> replayFriendApply(@RequestBody @Validated ChatFriendApplyReplayRequest request) {
        return ResultBean.success(chatFriendApplyService.replayFriendApply(request));
    }

    /**
     * 同意好友申请
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "同意好友申请")
    @PostMapping(value = "/agree")
    public ResultBean<String> agreeFriendApply(@RequestBody @Validated ChatFriendApplyAgreeRequest request) {
        return ResultBean.success(chatFriendApplyService.agreeFriendApply(request),"同意");
    }

    /**
     * 同意好友申请
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "删除好友申请")
    @PostMapping(value = "/delete")
    @Parameter(name = "applyId", description = "申请ID", required = true)
    public ResultBean<Boolean> deleteFriendApply(@RequestParam @NotNull(message = "申请ID不能为空") Integer applyId) {
        return ResultBean.success(chatFriendApplyService.deleteFriendApply(applyId));
    }

}
