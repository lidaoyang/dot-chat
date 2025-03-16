package com.dot.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.chat.dao.ChatFriendDao;
import com.dot.chat.model.ChatFriend;
import com.dot.chat.service.ChatFriendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 聊天室好友表服务接口实现
 * 
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Slf4j
@Service
public class ChatFriendServiceImpl extends ServiceImpl<ChatFriendDao, ChatFriend> implements ChatFriendService {

}
