package com.dot.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.chat.dao.ChatRoomDao;
import com.dot.chat.model.ChatRoom;
import com.dot.chat.service.ChatRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户聊天室表服务接口实现
 * 
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Slf4j
@Service
public class ChatRoomServiceImpl extends ServiceImpl<ChatRoomDao, ChatRoom> implements ChatRoomService {

}
