package com.dot.msg.chat.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.dot.comm.entity.ResultBean;
import com.dot.msg.chat.em.GroupSourceEm;
import com.dot.msg.chat.response.ChatGroupMemberSimResponse;
import com.dot.msg.chat.service.ChatGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 群组管理
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/10 18:11
 */
@Validated
@RestController
@RequestMapping("api/msg/chat/group")
@Tag(name = "群组管理")
public class ChatGroupController {

    @Resource
    private ChatGroupService chatGroupService;

    /**
     * 群成员列表
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "群成员列表")
    @GetMapping(value = "/member/list")
    @Parameters({
            @Parameter(name = "groupId", description = "群组ID", required = true),
            @Parameter(name = "keywords", description = "搜索关键词")
    })
    public ResultBean<List<ChatGroupMemberSimResponse>> getChatGroupMemberSimList(@RequestParam("groupId") @NotNull(message = "群组ID不能为空") Integer groupId,
                                                                                  @RequestParam(name = "keywords", required = false) String keywords) {
        return ResultBean.success(chatGroupService.getChatGroupMemberSimList(groupId, keywords));
    }

    /**
     * 创建群聊
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "创建群聊")
    @PostMapping(value = "/create")
    @Parameter(name = "members", description = "群组成员ID集合", required = true)
    public ResultBean<String> getChatGroupMemberSimList(@RequestParam("members") @NotEmpty(message = "群组成员ID集合不能为空") List<Integer> members) {
        return ResultBean.success(chatGroupService.createGroup(members), "创建成功");
    }

    /**
     * 更新群昵称
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "更新群昵称")
    @PostMapping(value = "/updateGroupNickname")
    @Parameters({
            @Parameter(name = "groupId", description = "群聊ID", required = true),
            @Parameter(name = "name", description = "在群里显示的昵称")
    })
    public ResultBean<Boolean> updateGroupNickname(@RequestParam("groupId") @NotNull(message = "群聊ID不能为空") Integer groupId,
                                                   @RequestParam(name = "name", required = false) String name) {
        return ResultBean.result(chatGroupService.updateGroupNickname(groupId, name));
    }

    /**
     * 更新群名称
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "更新群名称")
    @PostMapping(value = "/updateGroupName")
    @Parameters({
            @Parameter(name = "groupId", description = "群聊ID", required = true),
            @Parameter(name = "name", description = "群名称", required = true)})
    public ResultBean<Boolean> updateGroupName(@RequestParam("groupId") @NotNull(message = "群聊ID不能为空") Integer groupId,
                                               @RequestParam("name") @NotBlank(message = "群名称不能为空") String name) {
        return ResultBean.result(chatGroupService.updateGroupName(groupId, name));
    }

    /**
     * 更新群公告
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "更新群公告")
    @PostMapping(value = "/updateGroupNotice")
    @Parameters({
            @Parameter(name = "groupId", description = "群聊ID", required = true),
            @Parameter(name = "notice", description = "群公告", required = true)})
    public ResultBean<Boolean> updateGroupNotice(@RequestParam("groupId") @NotNull(message = "群聊ID不能为空") Integer groupId,
                                                 @RequestParam("notice") String notice) {
        return ResultBean.result(chatGroupService.updateGroupNotice(groupId, notice));
    }

    /**
     * 移除群成员
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "移除群成员")
    @PostMapping(value = "/removeGroupMember")
    @Parameters({
            @Parameter(name = "groupId", description = "群聊ID", required = true),
            @Parameter(name = "userIds", description = "群成员ID集合", required = true)})
    public ResultBean<Boolean> removeGroupMember(@RequestParam("groupId") @NotNull(message = "群组ID不能为空") Integer groupId,
                                                 @RequestParam("userIds") @NotEmpty(message = "群成员ID集合不能为空") List<Integer> userIds) {
        return ResultBean.result(chatGroupService.removeGroupMember(groupId, userIds));
    }

    /**
     * 退出群聊
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "退出群聊")
    @PostMapping(value = "/logoutGroup")
    @Parameter(name = "groupId", description = "群聊ID", required = true)
    public ResultBean<Boolean> logoutGroup(@RequestParam("groupId") @NotNull(message = "群组ID不能为空") Integer groupId) {
        return ResultBean.result(chatGroupService.logoutGroup(groupId));
    }

    /**
     * 添加群成员
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "添加群成员", description = "群主或管理员直接可以添加群成员,其他成员添加时,如果开启管理员确认,则走入群申请")
    @PostMapping(value = "/addGroupMember")
    @Parameters({
            @Parameter(name = "groupId", description = "群聊ID", required = true),
            @Parameter(name = "userIds", description = "好友ID集合", required = true),
            @Parameter(name = "source", description = "来源")})
    public ResultBean<Boolean> addGroupMember(@RequestParam("groupId") @NotNull(message = "群组ID不能为空") Integer groupId,
                                              @RequestParam("userIds") @NotEmpty(message = "好友ID集合不能为空") List<Integer> userIds,
                                              @RequestParam(name = "source", required = false) GroupSourceEm source) {
        return ResultBean.result(chatGroupService.addGroupMember(groupId, userIds, source));
    }

    /**
     * 申请加入群聊
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "申请加入群聊", description = "如果开启进群管理员确认,则走入群申请,反着直接加入")
    @PostMapping(value = "/applyJoinGroup")
    @Parameters({
            @Parameter(name = "groupId", description = "群聊ID", required = true),
            @Parameter(name = "source", description = "来源")})
    public ResultBean<Boolean> applyGroup(@RequestParam("groupId") @NotNull(message = "群组ID不能为空") Integer groupId,
                                          @RequestParam(name = "source", required = false) GroupSourceEm source) {
        return ResultBean.result(chatGroupService.applyGroup(groupId, source));
    }

    /**
     * 同意入群申请
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "同意入群申请")
    @PostMapping(value = "/agreeGroupApply")
    @Parameter(name = "applyId", description = "申请ID", required = true)
    public ResultBean<Boolean> agreeGroupApply(@RequestParam("applyId") @NotNull(message = "申请ID不能为空") Integer applyId) {
        return ResultBean.result(chatGroupService.agreeGroupApply(applyId));
    }


    /**
     * 获取群二维码
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "获取群二维码")
    @GetMapping(value = "/getGroupQrcode")
    @Parameter(name = "groupId", description = "群聊ID", required = true)
    public ResultBean<String> getGroupQrcode(@RequestParam("groupId") @NotNull(message = "群聊ID不能为空") Integer groupId) {
        return ResultBean.success(chatGroupService.getGroupQrcode(groupId), "获取群二维码成功");
    }

    /**
     * 设置群聊邀请确认标志
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "设置群聊邀请确认标志")
    @PostMapping(value = "/updateInviteCfm")
    @Parameters({
            @Parameter(name = "groupId", description = "聊ID", required = true),
            @Parameter(name = "flag", description = "群聊邀请确认标志", required = true)
    })
    public ResultBean<Boolean> updateIsTop(@RequestParam("groupId") @NotNull(message = "群聊ID不能为空") Integer groupId,
                                           @RequestParam("flag") @NotNull(message = "群聊邀请确认标志不能为空") Boolean flag) {
        return ResultBean.success(chatGroupService.updateInviteCfm(groupId, flag));
    }

    /**
     * 群主转让
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "群主转让")
    @PostMapping(value = "/groupLeaderTransfer")
    @Parameters({
            @Parameter(name = "groupId", description = "聊ID", required = true),
            @Parameter(name = "userId", description = "转让用户ID", required = true)
    })
    public ResultBean<Boolean> groupLeaderTransfer(@RequestParam("groupId") @NotNull(message = "群聊ID不能为空") Integer groupId,
                                                   @RequestParam("userId") @NotNull(message = "转让用户ID不能为空") Integer userId) {
        return ResultBean.success(chatGroupService.groupLeaderTransfer(groupId, userId));
    }

    /**
     * 解散群聊
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "解散群聊")
    @PostMapping(value = "/dissolveGroup")
    @Parameter(name = "groupId", description = "聊ID", required = true)
    public ResultBean<Boolean> dissolveGroup(@RequestParam("groupId") @NotNull(message = "群聊ID不能为空") Integer groupId) {
        return ResultBean.success(chatGroupService.dissolveGroup(groupId));
    }

    /**
     * 移除群管理员
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "移除群管理员")
    @PostMapping(value = "/removeGroupManager")
    @Parameters({
            @Parameter(name = "groupId", description = "聊ID", required = true),
            @Parameter(name = "userId", description = "转让用户ID", required = true)
    })
    public ResultBean<Boolean> removeGroupManager(@RequestParam("groupId") @NotNull(message = "群聊ID不能为空") Integer groupId,
                                                  @RequestParam("userId") @NotNull(message = "用户ID不能为空") Integer userId) {
        return ResultBean.success(chatGroupService.removeGroupManager(groupId, userId));
    }

    /**
     * 添加群管理员
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "添加群管理员")
    @PostMapping(value = "/addGroupManager")
    @Parameters({
            @Parameter(name = "groupId", description = "聊ID", required = true),
            @Parameter(name = "userId", description = "用户ID", required = true)
    })
    public ResultBean<Boolean> addGroupManager(@RequestParam("groupId") @NotNull(message = "群聊ID不能为空") Integer groupId,
                                               @RequestParam("userId") @NotNull(message = "用户ID不能为空") Integer userId) {
        return ResultBean.success(chatGroupService.addGroupManager(groupId, userId));
    }

}
