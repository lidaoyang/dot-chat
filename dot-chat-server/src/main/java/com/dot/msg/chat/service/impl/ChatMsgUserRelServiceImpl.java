package com.dot.msg.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.msg.chat.dao.ChatMsgUserRelDao;
import com.dot.msg.chat.model.ChatMsgUserRel;
import com.dot.msg.chat.service.ChatMsgUserRelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 聊天信息和用户关联表服务接口实现
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Slf4j
@Service
public class ChatMsgUserRelServiceImpl extends ServiceImpl<ChatMsgUserRelDao, ChatMsgUserRel> implements ChatMsgUserRelService {

    @Override
    public boolean cleanMsgList(Integer userId, String chatId) {
        LambdaQueryWrapper<ChatMsgUserRel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatMsgUserRel::getUserId, userId)
                .eq(ChatMsgUserRel::getChatId, chatId);
        return this.remove(queryWrapper);
    }

    @Override
    public ChatMsgUserRel getChatMsgUserRel(Integer userId, Integer msgId) {
        LambdaQueryWrapper<ChatMsgUserRel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatMsgUserRel::getUserId, userId)
                .eq(ChatMsgUserRel::getMsgId, msgId);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean deleteUserMsg(Integer userId, Integer msgId) {
        LambdaQueryWrapper<ChatMsgUserRel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatMsgUserRel::getUserId, userId)
                .eq(ChatMsgUserRel::getMsgId, msgId);
        return this.remove(queryWrapper);
    }

    @Override
    public boolean clearUserMsgByMsgId(Integer msgId) {
        LambdaQueryWrapper<ChatMsgUserRel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatMsgUserRel::getMsgId, msgId);
        return this.remove(queryWrapper);
    }

    @Override
    public ChatMsgUserRel getLastMsg(Integer userId) {
        LambdaQueryWrapper<ChatMsgUserRel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatMsgUserRel::getMsgId, ChatMsgUserRel::getCreateTime);
        queryWrapper.eq(ChatMsgUserRel::getUserId, userId);
        queryWrapper.orderByDesc(ChatMsgUserRel::getId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }
}
