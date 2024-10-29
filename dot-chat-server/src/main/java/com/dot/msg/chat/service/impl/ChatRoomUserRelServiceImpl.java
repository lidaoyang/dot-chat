package com.dot.msg.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.msg.chat.dao.ChatRoomUserRelDao;
import com.dot.msg.chat.model.ChatRoomUserRel;
import com.dot.msg.chat.service.ChatRoomUserRelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 聊天室和用户关联表服务接口实现
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Slf4j
@Service
public class ChatRoomUserRelServiceImpl extends ServiceImpl<ChatRoomUserRelDao, ChatRoomUserRel> implements ChatRoomUserRelService {

    @Override
    public List<ChatRoomUserRel> getChatRoomUserRelListByChatId(String chatId) {
        LambdaQueryWrapper<ChatRoomUserRel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatRoomUserRel::getChatId, chatId);
        return this.list(queryWrapper);
    }

    @Override
    public ChatRoomUserRel getChatRoomUserRel(String chatId, Integer userId) {
        LambdaQueryWrapper<ChatRoomUserRel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatRoomUserRel::getChatId, chatId);
        queryWrapper.eq(ChatRoomUserRel::getUserId, userId);
        return this.getOne(queryWrapper);
    }
}
