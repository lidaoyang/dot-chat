package com.dot.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.chat.dao.ChatRoomUserRelDao;
import com.dot.chat.model.ChatRoomUserRel;
import com.dot.chat.service.ChatRoomUserRelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 聊天室和用户关联表服务接口实现
 * 
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Slf4j
@Service
public class ChatRoomUserRelServiceImpl extends ServiceImpl<ChatRoomUserRelDao, ChatRoomUserRel> implements ChatRoomUserRelService {

}
