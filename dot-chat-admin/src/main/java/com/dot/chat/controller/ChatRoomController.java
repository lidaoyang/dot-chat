package com.dot.chat.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dot.chat.request.ChatRoomSearchRequest;
import com.dot.chat.response.*;
import com.dot.chat.service.ChatRoomService;
import com.dot.comm.entity.PageParam;
import com.dot.comm.entity.ResultBean;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * IM聊天室 -- 消息管理
 *
 * @author: Dao-yang.
 * @date: Created in 2022/12/22 17:25
 */

@Slf4j
@Validated
@RestController
@RequestMapping("api/chat/msg")
@Tag(name = "IM聊天室 -- 消息管理")
public class ChatRoomController {

    @Resource
    private ChatRoomService chatRoomService;

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "单聊消息列表")
    @GetMapping(value = "/single/list")
    public ResultBean<IPage<ChatRoomSingleResponse>> getSingleList(@Validated ChatRoomSearchRequest request,
                                                                   PageParam pageParam) {
        return ResultBean.success(chatRoomService.getSingleList(request, pageParam));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "群聊消息列表")
    @GetMapping(value = "/group/list")
    public ResultBean<IPage<ChatRoomGroupResponse>> getGroupList(@Validated ChatRoomSearchRequest request,
                                                                 PageParam pageParam) {
        return ResultBean.success(chatRoomService.getGroupList(request, pageParam));
    }

}
