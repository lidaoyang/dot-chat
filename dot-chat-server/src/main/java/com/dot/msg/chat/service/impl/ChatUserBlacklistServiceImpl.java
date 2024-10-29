package com.dot.msg.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.msg.chat.dao.ChatUserBlacklistDao;
import com.dot.msg.chat.model.ChatUserBlacklist;
import com.dot.msg.chat.service.ChatUserBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 聊天室用户黑名单表服务接口实现
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Slf4j
@Service
public class ChatUserBlacklistServiceImpl extends ServiceImpl<ChatUserBlacklistDao, ChatUserBlacklist> implements ChatUserBlacklistService {

}
