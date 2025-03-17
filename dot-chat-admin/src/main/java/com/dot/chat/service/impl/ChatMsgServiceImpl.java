package com.dot.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.chat.dao.ChatMsgDao;
import com.dot.chat.model.ChatMsg;
import com.dot.chat.response.ChatMsgResponse;
import com.dot.chat.response.ChatMsgUserResponse;
import com.dot.chat.response.ChatUserSimResponse;
import com.dot.chat.service.ChatMsgService;
import com.dot.chat.service.ChatUserService;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.exception.ApiException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天室消息记录表服务接口实现
 *
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Slf4j
@Service
public class ChatMsgServiceImpl extends ServiceImpl<ChatMsgDao, ChatMsg> implements ChatMsgService {

    @Resource
    private ChatUserService chatUserService;

    @Override
    public Map<String, List<ChatMsgResponse>> getMsgList(List<String> chatIds) {
        List<ChatMsg> chatMsgList = getChatMsgList(chatIds);
        if (CollUtil.isEmpty(chatMsgList)) {
            return Map.of();
        }
        List<ChatMsgResponse> responseList = BeanUtil.copyToList(chatMsgList, ChatMsgResponse.class);
        return responseList.stream().collect(Collectors.groupingBy(ChatMsgResponse::getChatId));
    }

    @Override
    public Map<String, List<ChatMsgUserResponse>> getMsgUserList(List<String> chatIds) {
        List<ChatMsg> chatMsgList = getChatMsgList(chatIds);
        if (CollUtil.isEmpty(chatMsgList)) {
            return Map.of();
        }
        List<Integer> uidList = chatMsgList.stream().map(ChatMsg::getSendUserId).distinct().toList();
        Map<Integer, ChatUserSimResponse> userSimMap = chatUserService.getUserSimMap(uidList);
        if (CollUtil.isEmpty(userSimMap)) {
            return Map.of();
        }
        List<ChatMsgUserResponse> responseList = BeanUtil.copyToList(chatMsgList, ChatMsgUserResponse.class);
        responseList.forEach(response -> {
            response.setUser(userSimMap.get(response.getSendUserId()));
        });
        return responseList.stream().collect(Collectors.groupingBy(ChatMsgUserResponse::getChatId));
    }

    private List<ChatMsg> getChatMsgList(List<String> chatIds) {
        if (CollUtil.isEmpty(chatIds)) {
            return List.of();
        }
        LambdaQueryWrapper<ChatMsg> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatMsg::getChatId, ChatMsg::getId, ChatMsg::getSendUserId, ChatMsg::getMsgType, ChatMsg::getMsg, ChatMsg::getSendTime, ChatMsg::getDeviceType);
        queryWrapper.in(ChatMsg::getChatId, chatIds);
        queryWrapper.orderByAsc(ChatMsg::getId);
        queryWrapper.last("limit 50");
        return this.list(queryWrapper);
    }
}
