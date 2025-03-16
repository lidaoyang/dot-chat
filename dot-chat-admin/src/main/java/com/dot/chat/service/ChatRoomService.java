package com.dot.chat.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.chat.model.ChatRoom;
import com.dot.chat.request.ChatRoomSearchRequest;
import com.dot.chat.response.ChatRoomGroupResponse;
import com.dot.chat.response.ChatRoomSingleResponse;
import com.dot.comm.entity.PageParam;

/**
 * 用户聊天室表服务接口
 *
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
public interface ChatRoomService extends IService<ChatRoom> {

    /**
     * 获取聊天室列表
     *
     * @param request   请求参数
     * @param pageParam 分页参数
     * @return 聊天室列表
     */
    IPage<ChatRoomSingleResponse> getSingleList(ChatRoomSearchRequest request, PageParam pageParam);

    /**
     * 获取聊天室列表
     *
     * @param request   请求参数
     * @param pageParam 分页参数
     * @return 聊天室列表
     */
    IPage<ChatRoomGroupResponse> getGroupList(ChatRoomSearchRequest request, PageParam pageParam);


}
