package com.dot.msg.chat.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.dot.comm.entity.ResultBean;
import com.dot.msg.chat.response.ChatUserFriendInfoResponse;
import com.dot.msg.chat.response.ChatUserFriendResponse;
import com.dot.msg.chat.service.ChatFriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天好友管理
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/10 18:11
 */
@Validated
@RestController
@RequestMapping("api/msg/chat/friend")
@Tag(name = "聊天好友管理")
public class ChatFriendController {

    @Resource
    private ChatFriendService chatFriendService;


    /**
     * 获取当前聊天用户
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "获取好友列表")
    @GetMapping(value = "/list")
    @Parameter(name = "keywords", description = "关键词搜索(ID,nickname)")
    public ResultBean<List<ChatUserFriendResponse>> getChatUserFriendList(@RequestParam(name = "keywords", required = false) String keywords) {
        return ResultBean.success(chatFriendService.getChatUserFriendList(keywords));
    }


    /**
     * 选择好友的列表
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "选择好友的列表", description = "创建或添加到群聊时选择好友列表")
    @GetMapping(value = "/choose/list")
    @Parameters({
            @Parameter(name = "filterGroupId", description = "过滤群组ID"),
            @Parameter(name = "keywords", description = "关键词搜索(ID,nickname)")
    })
    public ResultBean<List<ChatUserFriendResponse>> getChatUserFriendList(@RequestParam(name = "filterGroupId", required = false) Integer filterGroupId,
                                                                          @RequestParam(name = "keywords", required = false) String keywords) {
        return ResultBean.success(chatFriendService.getChatUserFriendList(filterGroupId, keywords));
    }

    /**
     * 获取当前聊天用户好友详情
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "获取好友详情")
    @GetMapping(value = "/info")
    @Parameter(name = "friendId", description = "好友ID", required = true)
    public ResultBean<ChatUserFriendInfoResponse> getChatUserFriendInfo(@RequestParam("friendId") @NotNull(message = "好友ID不能为空") Integer friendId) {
        return ResultBean.success(chatFriendService.getChatUserFriendInfo(friendId));
    }

    /**
     * 修改好友备注
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "修改好友备注")
    @PostMapping(value = "/modifyRemark")
    @Parameters({
            @Parameter(name = "friendId", description = "好友ID", required = true),
            @Parameter(name = "remark", description = "好友备注")
    })
    public ResultBean<String> modifyFriendRemark(@RequestParam("friendId") @NotNull(message = "好友ID不能为空") Integer friendId,
                                                 @RequestParam(name = "remark", required = false) String remark) {
        return ResultBean.success(chatFriendService.modifyFriendRemark(friendId, remark), "修改成功");
    }
}
