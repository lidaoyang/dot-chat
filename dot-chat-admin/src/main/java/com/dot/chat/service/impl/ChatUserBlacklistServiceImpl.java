package com.dot.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.chat.dao.ChatUserBlacklistDao;
import com.dot.chat.model.ChatUserBlacklist;
import com.dot.chat.service.ChatUserBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 聊天室用户黑名单表服务接口实现
 * 
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Slf4j
@Service
public class ChatUserBlacklistServiceImpl extends ServiceImpl<ChatUserBlacklistDao, ChatUserBlacklist> implements ChatUserBlacklistService {

}
