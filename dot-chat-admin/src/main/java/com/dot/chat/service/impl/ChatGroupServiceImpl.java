package com.dot.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.chat.dao.ChatGroupDao;
import com.dot.chat.model.ChatGroup;
import com.dot.chat.response.ChatGroupResponse;
import com.dot.chat.service.ChatGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 聊天室群组表服务接口实现
 *
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Slf4j
@Service
public class ChatGroupServiceImpl extends ServiceImpl<ChatGroupDao, ChatGroup> implements ChatGroupService {


    @Override
    public Map<Integer, ChatGroupResponse> getGroupMap(Collection<Integer> groupIdList) {
        if (CollUtil.isEmpty(groupIdList)) {
            return Map.of();
        }
        LambdaQueryWrapper<ChatGroup> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatGroup::getId, ChatGroup::getName, ChatGroup::getAvatar, ChatGroup::getMemberCount, ChatGroup::getIsDissolve, ChatGroup::getDissolveTime);
        queryWrapper.in(ChatGroup::getId, groupIdList);
        List<ChatGroup> groupList = this.list(queryWrapper);
        List<ChatGroupResponse> responseList = BeanUtil.copyToList(groupList, ChatGroupResponse.class);
        return responseList.stream().collect(Collectors.toMap(ChatGroupResponse::getId, Function.identity()));
    }
}
