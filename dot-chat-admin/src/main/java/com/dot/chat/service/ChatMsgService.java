package com.dot.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.chat.model.ChatMsg;
import com.dot.chat.response.ChatMsgResponse;
import com.dot.chat.response.ChatMsgUserResponse;

import java.util.List;
import java.util.Map;

/**
 * 聊天室消息记录表服务接口
 *
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
public interface ChatMsgService extends IService<ChatMsg> {

    /**
     * 获取聊天室消息记录
     *
     * @param chatIds 聊天室ID
     * @return List<ChatMsgResponse> map
     */
    Map<String, List<ChatMsgResponse>> getMsgList(List<String> chatIds);

    /**
     * 获取聊天室消息记录(包含用户信息)
     *
     * @param chatIds 聊天室ID
     * @return List<ChatMsgUserResponse> map
     */
    Map<String, List<ChatMsgUserResponse>> getMsgUserList(List<String> chatIds);
}
