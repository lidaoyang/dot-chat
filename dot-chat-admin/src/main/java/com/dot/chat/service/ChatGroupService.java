package com.dot.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.chat.model.ChatGroup;
import com.dot.chat.response.ChatGroupResponse;

import java.util.Collection;
import java.util.Map;

/**
 * 聊天室群组表服务接口
 * 
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
public interface ChatGroupService extends IService<ChatGroup> {

    Map<Integer, ChatGroupResponse> getGroupMap(Collection<Integer> groupIdList);
}
