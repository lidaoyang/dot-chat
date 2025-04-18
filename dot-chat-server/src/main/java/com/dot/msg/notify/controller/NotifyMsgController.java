package com.dot.msg.notify.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.dot.comm.entity.ResultBean;
import com.dot.msg.notify.request.EntNotifyMsgSendRequest;
import com.dot.msg.notify.response.EntNotifyMsgResponse;
import com.dot.msg.notify.service.NotifyMsgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知管理
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/10 18:11
 */
@Validated
@RestController
@RequestMapping("api/msg/notify/msg")
@Tag(name = "通知管理")
public class NotifyMsgController {

    @Resource
    private NotifyMsgService notifyMsgService;

    /**
     * 通知信息列表
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "通知信息列表", description = "获取当前登录用户的通知信息列表(企业或供应商通知信息)")
    @GetMapping(value = "/notifylist")
    @Parameter(name = "limit", description = "一次最多加载条数,默认50")
    public ResultBean<List<EntNotifyMsgResponse>> getNotifyMsgNotifyList(@RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        return ResultBean.success(notifyMsgService.getNotifyMsgNotifyList(limit));
    }

    /**
     * 发送企业通知信息
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "发送企业通知信息")
    @PostMapping(value = "/sendNotifyMsg")
    public ResultBean<Boolean> sendEntNotifyMsg(@RequestBody @Validated EntNotifyMsgSendRequest sendRequest) {
        return ResultBean.success(notifyMsgService.sendEntNotifyMsg(sendRequest));
    }

    /**
     * 更新已读
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "更新已读")
    @PostMapping(value = "/updateToRead")
    @Parameter(name = "id", description = "通知信息ID", required = true)
    public ResultBean<Boolean> updateIsRead(@RequestParam("id") @NotNull(message = "通知信息ID不能为空") Integer id) {
        return ResultBean.success(notifyMsgService.updateIsRead(id));
    }


    /**
     * 全部已读
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "全部已读")
    @PostMapping(value = "/allRead")
    public ResultBean<Boolean> allRead() {
        return ResultBean.success(notifyMsgService.allRead());
    }
}
