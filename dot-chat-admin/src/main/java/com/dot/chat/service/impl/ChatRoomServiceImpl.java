package com.dot.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.chat.dao.ChatRoomDao;
import com.dot.chat.dto.ChatRoomDto;
import com.dot.chat.em.ChatTypeEm;
import com.dot.chat.model.ChatRoom;
import com.dot.chat.request.ChatRoomSearchRequest;
import com.dot.chat.response.*;
import com.dot.chat.service.ChatGroupService;
import com.dot.chat.service.ChatMsgService;
import com.dot.chat.service.ChatRoomService;
import com.dot.chat.service.ChatUserService;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.entity.PageParam;
import com.dot.comm.exception.ApiException;
import com.dot.comm.utils.PageUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户聊天室表服务接口实现
 *
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Slf4j
@Service
public class ChatRoomServiceImpl extends ServiceImpl<ChatRoomDao, ChatRoom> implements ChatRoomService {

    @Resource
    private ChatUserService chatUserService;

    @Resource
    private ChatGroupService chatGroupService;

    @Resource
    private ChatMsgService chatMsgService;


    @Override
    public IPage<ChatRoomSingleResponse> getSingleList(ChatRoomSearchRequest request, PageParam pageParam) {
        IPage<ChatRoomDto> page = getChatRoomIPage(ChatTypeEm.SINGLE, request, pageParam);
        if (page.getRecords().isEmpty()) {
            return PageUtil.copyPage(page, new ArrayList<>());
        }
        List<ChatRoomDto> records = page.getRecords();
        List<String> chatIdList = records.stream().map(ChatRoomDto::getChatId).toList();

        // 获取聊天室消息记录
        Map<String, List<ChatMsgResponse>> msgUserListMap = chatMsgService.getMsgList(chatIdList);

        // 获取聊天室用户信息
        List<Integer> uids = chatIdList.stream().flatMap(chatId -> getChatUserIdsByChatId(chatId).stream()).distinct().toList();
        Map<Integer, ChatUserSimResponse> userSimMap = chatUserService.getUserSimMap(uids);

        List<ChatRoomSingleResponse> responseList = new ArrayList<>();
        records.forEach(chatRoom -> {
            ChatRoomSingleResponse response = new ChatRoomSingleResponse();
            response.setChatId(chatRoom.getChatId());
            response.setLastTime(chatRoom.getLastTime());
            List<Integer> chatUids = getChatUserIdsByChatId(chatRoom.getChatId());
            response.setUser1(userSimMap.get(chatUids.get(0)));
            response.setUser2(userSimMap.get(chatUids.get(1)));
            response.setMsgList(msgUserListMap.getOrDefault(chatRoom.getChatId(), new ArrayList<>()));
            responseList.add(response);
        });

        return PageUtil.copyPage(page, responseList);
    }

    @Override
    public IPage<ChatRoomGroupResponse> getGroupList(ChatRoomSearchRequest request, PageParam pageParam) {
        IPage<ChatRoomDto> page = getChatRoomIPage(ChatTypeEm.GROUP, request, pageParam);
        if (page.getRecords().isEmpty()) {
            return PageUtil.copyPage(page, new ArrayList<>());
        }
        List<ChatRoomDto> records = page.getRecords();
        // 获取群组信息
        List<Integer> groupIds = records.stream().map(ChatRoomDto::getGroupId).distinct().toList();
        Map<Integer, ChatGroupResponse> groupMap = chatGroupService.getGroupMap(groupIds);

        // 获取聊天室消息记录
        List<String> chatIdList = records.stream().map(ChatRoomDto::getChatId).toList();
        Map<String, List<ChatMsgUserResponse>> msgUserListMap = chatMsgService.getMsgUserList(chatIdList);

        List<ChatRoomGroupResponse> responseList = new ArrayList<>();
        records.forEach(chatRoom -> {
            ChatRoomGroupResponse response = new ChatRoomGroupResponse();
            response.setChatId(chatRoom.getChatId());
            response.setGroup(groupMap.get(chatRoom.getGroupId()));
            response.setMsgList(msgUserListMap.getOrDefault(chatRoom.getChatId(), new ArrayList<>()));
            responseList.add(response);
        });

        return PageUtil.copyPage(page, responseList);
    }

    private IPage<ChatRoomDto> getChatRoomIPage(ChatTypeEm chatTypeEm, ChatRoomSearchRequest request,
                                             PageParam pageParam) {
        QueryWrapper<ChatRoom> queryWrapper = Wrappers.query();
        queryWrapper.eq("cr.chat_type", chatTypeEm.name());
        queryWrapper.eq(ObjectUtil.isNotNull(request.getUserId()),"crur.user_id", request.getUserId());
        queryWrapper.ge(StringUtils.isNotBlank(request.getStartDate()), "crur.last_time", request.getStartDate() + " 00:00:00");
        queryWrapper.le(StringUtils.isNotBlank(request.getEntDate()), "crur.last_time", request.getEntDate() + " 23:59:59");
        queryWrapper.orderByDesc("crur.last_time").orderByDesc("cr.id");
        return baseMapper.selectChatRoomList(Page.of(pageParam.getPageIndex(), pageParam.getPageSize()), queryWrapper);
    }

    public static List<Integer> getChatUserIdsByChatId(String chatId) {
        if (chatId.contains("G")) {
            log.error("群聊ID:{}", chatId);
            throw new ApiException(ExceptionCodeEm.PRAM_NOT_MATCH, "只支持单聊ID");
        }
        String[] userIds = chatId.split("_");
        return CollUtil.newArrayList(Integer.valueOf(userIds[0]), Integer.valueOf(userIds[1]));
    }

}
