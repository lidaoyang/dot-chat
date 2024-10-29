package com.dot.msg.chat.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.dot.comm.em.UserTypeEm;
import com.dot.comm.entity.ResultBean;
import com.dot.msg.chat.response.ChatUserFriendInfoResponse;
import com.dot.msg.chat.response.ChatUserFriendResponse;
import com.dot.msg.chat.service.ChatFriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
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
    @Parameters({
            @Parameter(name = "userType", description = "用户类型", required = true),
            @Parameter(name = "keywords", description = "关键词搜索(ID,nickname)")
    })
    public ResultBean<List<ChatUserFriendResponse>> getChatUserFriendList(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType,
                                                                          @RequestParam(required = false) String keywords) {
        return ResultBean.success(chatFriendService.getChatUserFriendList(userType, keywords));
    }


    /**
     * 选择好友的列表
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "选择好友的列表", description = "创建或添加到群聊时选择好友列表")
    @GetMapping(value = "/choose/list")
    @Parameters({
            @Parameter(name = "userType", description = "用户类型", required = true),
            @Parameter(name = "filterGroupId", description = "过滤群组ID"),
            @Parameter(name = "keywords", description = "关键词搜索(ID,nickname)")
    })
    public ResultBean<List<ChatUserFriendResponse>> getChatUserFriendList(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType,
                                                                          @RequestParam(required = false) Integer filterGroupId,
                                                                          @RequestParam(required = false) String keywords) {
        return ResultBean.success(chatFriendService.getChatUserFriendList(userType, filterGroupId, keywords));
    }

    /**
     * 获取当前聊天用户好友详情
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "获取好友详情")
    @GetMapping(value = "/info")
    @Parameters({
            @Parameter(name = "userType", description = "用户类型", required = true),
            @Parameter(name = "friendId", description = "好友ID", required = true)
    })
    public ResultBean<ChatUserFriendInfoResponse> getChatUserFriendInfo(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType,
                                                                        @RequestParam @NotNull(message = "好友ID不能为空") Integer friendId) {
        return ResultBean.success(chatFriendService.getChatUserFriendInfo(userType, friendId));
    }

    /**
     * 修改好友备注
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "修改好友备注")
    @PostMapping(value = "/modifyRemark")
    @Parameters({
            @Parameter(name = "userType", description = "用户类型", required = true),
            @Parameter(name = "friendId", description = "好友ID", required = true),
            @Parameter(name = "remark", description = "好友备注")
    })
    public ResultBean<String> modifyFriendRemark(@RequestParam @NotNull(message = "用户类型不能为空") UserTypeEm userType,
                                                 @RequestParam @NotNull(message = "好友ID不能为空") Integer friendId,
                                                 @RequestParam(required = false) String remark) {
        return ResultBean.success(chatFriendService.modifyFriendRemark(userType, friendId, remark), "修改成功");
    }
}
