package com.dot.msg.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.comm.em.UserTypeEm;
import com.dot.msg.chat.dao.ChatGroupMemberDao;
import com.dot.msg.chat.dto.ChatGroupInfoDto;
import com.dot.msg.chat.dto.ChatGroupMemberSimDto;
import com.dot.msg.chat.em.GroupSourceEm;
import com.dot.msg.chat.model.ChatGroupMember;
import com.dot.msg.chat.model.ChatUser;
import com.dot.msg.chat.service.ChatGroupMemberService;
import com.dot.msg.chat.service.ChatUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天室群成员表服务接口实现
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Slf4j
@Service
public class ChatGroupMemberServiceImpl extends ServiceImpl<ChatGroupMemberDao, ChatGroupMember> implements ChatGroupMemberService {

    @Resource
    private ChatUserService chatUserService;

    @Override
    public List<ChatGroupMember> getChatGroupMemberListByUserId(Integer userId) {
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatGroupMember::getUserId, userId);
        return this.list(queryWrapper);
    }

    @Override
    public List<ChatGroupMember> getChatGroupMemberListByGroupId(Integer groupId) {
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatGroupMember::getGroupId, groupId);
        return this.list(queryWrapper);
    }

    @Override
    public List<Integer> getChatGroupMemberIdListByGroupId(Integer groupId) {
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatGroupMember::getUserId);
        queryWrapper.eq(ChatGroupMember::getGroupId, groupId);
        return listObjs(queryWrapper, obj -> (Integer) obj);
    }

    @Override
    public List<Integer> getChatGroupMemberIdListByGroupId(Integer groupId, List<Integer> userIds) {
        if (CollUtil.isNotEmpty(userIds)) {
            LambdaQueryWrapper<ChatGroupMember> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.select(ChatGroupMember::getUserId);
            queryWrapper.eq(ChatGroupMember::getGroupId, groupId);
            queryWrapper.in(ChatGroupMember::getUserId, userIds);
            return listObjs(queryWrapper, obj -> (Integer) obj);
        }
        return null;
    }

    @Override
    public List<ChatGroupMemberSimDto> getChatGroupMemberSimList(UserTypeEm userType, Integer groupId, String keywords) {
        Integer chatUserId = chatUserService.getCurrentChatUserId(userType);
        List<ChatGroupMemberSimDto> groupMemberList = getChatGroupMemberList(chatUserId, groupId, keywords);
        setSourceDesc(groupMemberList);
        return groupMemberList;
    }

    private void setSourceDesc(List<ChatGroupMemberSimDto> groupMemberList) {
        List<ChatUser> chatUserList = getChatUserList(groupMemberList);
        groupMemberList.forEach(m -> {
            if (m.getSource().equals(GroupSourceEm.INVITE.name())) {
                chatUserList.stream().filter(c -> c.getId().equals(m.getInviteUserId())).findFirst().ifPresent(c -> m.setSourceDesc(c.getNickname() + GroupSourceEm.INVITE.getDesc()));
            } else {
                m.setSourceDesc(GroupSourceEm.getDesc(m.getSource()));
            }
        });
    }

    private List<ChatUser> getChatUserList(List<ChatGroupMemberSimDto> memberSimDtoList) {
        List<Integer> inviteUserIds = memberSimDtoList.stream().filter(m -> m.getSource().equals(GroupSourceEm.INVITE.name())).map(ChatGroupMemberSimDto::getInviteUserId).distinct().collect(Collectors.toList());
        List<ChatUser> chatUserList = new ArrayList<>();
        if (CollUtil.isNotEmpty(inviteUserIds)) {
            chatUserList = chatUserService.getSimList(inviteUserIds);
        }
        return chatUserList;
    }

    @Override
    public List<ChatGroupMemberSimDto> getChatGroupMemberList(Integer userId, Integer groupId, String keywords) {
        String andSql = "";
        if (StringUtils.isNotBlank(keywords)) {
            String keyword = "'%" + keywords + "%'";
            andSql = "and (cgm.nickname like " + keyword + " or cf.remark like " + keyword + " or cu.nickname like " + keyword + " or cu.phone like " + keyword + ")";
        }
        return baseMapper.selectChatGroupMemberSimList(groupId, userId, andSql);
    }


    @Override
    public List<String> getGroupMemberAvaterList(Integer groupId) {
        return baseMapper.selectChatGroupMemberAvaterList(groupId);
    }

    @Override
    public ChatGroupInfoDto getChatGroupInfo(Integer groupId, Integer userId) {
        return baseMapper.selectChatGroupInfo(groupId, userId);
    }
}
