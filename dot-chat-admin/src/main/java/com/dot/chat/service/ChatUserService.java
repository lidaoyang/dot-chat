package com.dot.chat.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.chat.model.ChatUser;
import com.dot.chat.request.ChatUserSearchRequest;
import com.dot.chat.response.ChatUserRegisterCountResponse;
import com.dot.chat.response.ChatUserResponse;
import com.dot.chat.response.ChatUserSimResponse;
import com.dot.comm.entity.PageParam;

import java.util.List;

/**
 * 聊天室用户表(关联管理员表和企业用户表)服务接口
 *
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
public interface ChatUserService extends IService<ChatUser> {

    /**
     * 获取聊天室用户列表
     *
     * @param request   请求参数
     * @param pageParam 分页参数
     * @return 分页结果
     */
    IPage<ChatUserResponse> getList(ChatUserSearchRequest request, PageParam pageParam);

    /**
     * 更新用户状态
     *
     * @param id     用户id
     * @param status 用户状态(true:正常,false:禁用)
     * @return 是否成功
     */
    Boolean updateUserStatus(Integer id, Boolean status);

    /**
     * 获取用户注册数量
     *
     * @return 用户注册数量
     */
    List<ChatUserRegisterCountResponse> getUserRegisterCount();

    /**
     * 获取用户列表-精简
     *
     * @param date 日期
     * @return 用户列表
     */
    List<ChatUserSimResponse> getUserSimList(String date);
}
