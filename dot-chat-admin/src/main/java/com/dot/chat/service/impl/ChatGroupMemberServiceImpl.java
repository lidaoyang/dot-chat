package com.dot.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.chat.dao.ChatGroupMemberDao;
import com.dot.chat.model.ChatGroupMember;
import com.dot.chat.service.ChatGroupMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 聊天室群成员表服务接口实现
 * 
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Slf4j
@Service
public class ChatGroupMemberServiceImpl extends ServiceImpl<ChatGroupMemberDao, ChatGroupMember> implements ChatGroupMemberService {

}
