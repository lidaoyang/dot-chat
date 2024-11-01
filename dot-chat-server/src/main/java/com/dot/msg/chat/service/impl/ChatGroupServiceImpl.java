package com.dot.msg.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.comm.constants.CommConstant;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.entity.OSSConfig;
import com.dot.comm.exception.ApiException;
import com.dot.comm.utils.*;
import com.dot.msg.chat.config.ChatConfig;
import com.dot.msg.chat.dao.ChatGroupDao;
import com.dot.msg.chat.dto.ChatGroupInfoDto;
import com.dot.msg.chat.dto.ChatGroupMemberSimDto;
import com.dot.msg.chat.dto.ChatUserSimDto;
import com.dot.msg.chat.em.GroupSourceEm;
import com.dot.msg.chat.listener.event.*;
import com.dot.msg.chat.model.ChatGroup;
import com.dot.msg.chat.model.ChatGroupApply;
import com.dot.msg.chat.model.ChatGroupApplyUserRel;
import com.dot.msg.chat.model.ChatGroupMember;
import com.dot.msg.chat.response.ChatGroupMemberSimResponse;
import com.dot.msg.chat.response.ChatUserFriendResponse;
import com.dot.msg.chat.response.ChatUserResponse;
import com.dot.msg.chat.service.*;
import com.dot.msg.chat.tio.util.TioUtil;
import com.dot.sys.upload.config.UploadFileConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天室群组表服务接口实现
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Slf4j
@Service
public class ChatGroupServiceImpl extends ServiceImpl<ChatGroupDao, ChatGroup> implements ChatGroupService {


    @Resource
    private ChatUserService chatUserService;

    @Resource
    private ChatFriendService chatFriendService;

    @Resource
    private ChatGroupMemberService chatGroupMemberService;

    @Resource
    private ChatGroupApplyService chatGroupApplyService;

    @Resource
    private ChatGroupApplyUserRelService chatGroupApplyUserRelService;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private ChatConfig chatConfig;

    @Resource(name = "videoConfig")
    private UploadFileConfig videoConfig;

    @Resource(name = "redisUtil")
    private RedisUtil redisUtil;

    @Resource
    private OSSConfig ossConfig;

    @Override
    public List<ChatGroup> getList(List<Integer> groupIds) {
        if (CollUtil.isEmpty(groupIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ChatGroup> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(ChatGroup::getId, groupIds);
        return this.list(queryWrapper);
    }

    @Override
    public List<ChatGroupMemberSimResponse> getChatGroupMemberSimList(Integer groupId, String keywords) {
        List<ChatGroupMemberSimDto> groupMemberSimList = chatGroupMemberService.getChatGroupMemberSimList(groupId, keywords);
        return BeanUtil.copyToList(groupMemberSimList, ChatGroupMemberSimResponse.class);
    }

    @Override
    public String createGroup(List<Integer> members) {
        checkMembers(members);
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        List<ChatUserFriendResponse> userFriendList = getChatUserFriendList(members, chatUser);
        String nicknameStr = getNicknameStr(userFriendList, chatUser);
        ChatGroup chatGroup = getNewChatGroup(userFriendList, chatUser, nicknameStr);
        List<ChatGroupMember> groupMemberList = getNewGroupMemberList(chatUser, userFriendList);
        transactionTemplate.execute(t -> {
            // 保存群组
            saveGroup(chatGroup, chatUser);
            // 保存群成员
            saveGroupMember(groupMemberList, chatGroup, chatUser);
            List<Integer> groupMemberIds = groupMemberList.stream().map(ChatGroupMember::getUserId).collect(Collectors.toList());
            groupMemberIds.remove(chatUser.getId());
            // 发送创建群聊事件->添加聊天室
            eventPublisher.publishEvent(new ChatGroupCreateEvent(chatGroup.getId(), chatUser.getId(), nicknameStr, groupMemberIds));
            return true;
        });

        return TioUtil.generateChatId(chatGroup.getId());
    }

    private void checkMembers(List<Integer> members) {
        if (CollUtil.isEmpty(members)) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "群成员列表不能为空");
        }
        if (members.size() < 2) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "群成员数不能少于2个");
        }
    }

    private List<ChatUserFriendResponse> getChatUserFriendList(List<Integer> members, ChatUserResponse chatUser) {
        List<ChatUserFriendResponse> userFriendList = chatFriendService.getChatUserFriendList(chatUser.getId(), members);
        if (CollUtil.isEmpty(userFriendList)) {
            log.error("群成员用户不存在,members:{}", members);
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "群成员用户不存在");
        }
        return userFriendList;
    }

    private ChatGroup getNewChatGroup(List<ChatUserFriendResponse> userFriendList, ChatUserResponse chatUser, String nicknameStr) {
        List<String> avatarList = userFriendList.stream().map(ChatUserFriendResponse::getAvatar).collect(Collectors.toList());
        avatarList.add(chatUser.getAvatar());
        ChatGroup chatGroup = new ChatGroup();
        String groupName = nicknameStr.length() > 127 ? nicknameStr.substring(0, 127) : nicknameStr;
        chatGroup.setName(groupName);
        chatGroup.setGroupLeaderId(chatUser.getId());
        chatGroup.setAvatar(generateHeadAndUpload(avatarList, chatUser.getId()));
        chatGroup.setMemberCount(userFriendList.size() + 1);
        return chatGroup;
    }

    private List<ChatGroupMember> getNewGroupMemberList(ChatUserResponse chatUser, List<ChatUserFriendResponse> userFriendList) {
        List<ChatGroupMember> groupMemberList = new ArrayList<>();
        ChatGroupMember groupMemberLeader = new ChatGroupMember();
        groupMemberLeader.setUserId(chatUser.getId());
        groupMemberLeader.setIsGroupLeader(true);
        groupMemberLeader.setSource( GroupSourceEm.CREATE.name());
        groupMemberList.add(groupMemberLeader);
        userFriendList.forEach(friend -> {
            ChatGroupMember groupMember = new ChatGroupMember();
            groupMember.setUserId(friend.getFriendId());
            groupMember.setSource( GroupSourceEm.INVITE.name());
            groupMemberList.add(groupMember);
        });
        return groupMemberList;
    }

    private void saveGroup(ChatGroup chatGroup, ChatUserResponse chatUser) {
        boolean saved = this.save(chatGroup);
        if (!saved) {
            log.error("创建群组失败,chatUserId:{},chatGroup:{}", chatUser.getId(), chatGroup);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "创建群组失败");
        }
    }

    private void saveGroupMember(List<ChatGroupMember> groupMemberList, ChatGroup chatGroup, ChatUserResponse chatUser) {
        groupMemberList.forEach(member -> member.setGroupId(chatGroup.getId()));
        boolean saved1 = chatGroupMemberService.saveBatch(groupMemberList);
        if (!saved1) {
            log.error("添加群成员失败,chatUserId:{},groupMemberList:{}", chatUser.getId(), groupMemberList);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "创建群组失败");
        }
    }

    private String getNicknameStr(List<ChatUserFriendResponse> userFriendList, ChatUserResponse chatUser) {
        List<String> nicknameList = userFriendList.stream().map(ChatUserFriendResponse::getNickname).collect(Collectors.toList());
        nicknameList.add(chatUser.getNickname());
        return String.join("、", nicknameList);
    }

    private String generateHeadAndUpload(List<String> avatarList, Integer chatUserId) {
        if (avatarList.size() > 9) {
            avatarList.subList(0, 9);
        }
        InputStream headIs = MakeGroupHeadPicUtil.getCombinationOfhead(avatarList);
        String type = "chatgroup";
        String filename = CommUtil.getUlid() + ".png";
        String uploadPath = "image/" + type + "/" + chatUserId + "/" + filename;
        if (ossConfig.isLocal()){
            String targetPath = videoConfig.getRootPath() + "/" + uploadPath;
            log.info("文件流保存到本地,targetPath:{}", targetPath);
            FileUtil.saveToFile(headIs, targetPath);
        } else {
            // 上传图片到oss
            OSSUtil.getInstance(ossConfig).upload(headIs, uploadPath);
        }
        return ossConfig.getDomain() + "/" + uploadPath;
    }

    @Override
    public Boolean updateGroupNickname(Integer groupId, String nickname) {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        LambdaUpdateWrapper<ChatGroupMember> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, chatUserId)
                .set(ChatGroupMember::getNickname, nickname);
        return chatGroupMemberService.update(updateWrapper);
    }

    @Override
    public Boolean updateGroupName(Integer groupId, String groupName) {
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        ChatGroupInfoDto chatGroupInfo = getChatGroupInfoDto(groupId, chatUser);
        return transactionTemplate.execute(t -> {
            boolean ret = updateGroupName2(groupId, groupName);
            if (!ret) {
                log.error("修改群名称失败,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "修改群名称失败");
            }
            String nickname = StringUtils.isBlank(chatGroupInfo.getNickname()) ? chatUser.getNickname() : chatGroupInfo.getNickname();
            eventPublisher.publishEvent(new ChatGroupNameUpdateEvent(groupId, groupName, chatUser.getId(), nickname));
            return true;
        });
    }

    private boolean updateGroupName2(Integer groupId, String groupName) {
        LambdaUpdateWrapper<ChatGroup> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatGroup::getId, groupId)
                .set(ChatGroup::getName, groupName);
        return this.update(updateWrapper);
    }

    @Override
    public Boolean updateGroupNotice(Integer groupId, String notice) {
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        ChatGroupInfoDto chatGroupInfo = getChatGroupInfoDto(groupId, chatUser);
        return transactionTemplate.execute(t -> {
            boolean ret = updateGroupNotice(groupId, chatUser.getId(), notice);
            if (!ret) {
                log.error("修改群公告失败,chatUserId:{},groupId:{},notice:{}", chatUser.getId(), groupId, notice);
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "修改群公告失败");
            }
            if (StringUtils.isNotBlank(notice)) {
                String nickname = StringUtils.isBlank(chatGroupInfo.getNickname()) ? chatUser.getNickname() : chatGroupInfo.getNickname();
                eventPublisher.publishEvent(new ChatGroupNoticeUpdateEvent(groupId, chatUser.getId(), nickname));
            }
            return true;
        });
    }

    private boolean updateGroupNotice(Integer groupId, Integer userId, String notice) {
        String noticeContent = null;
        JSONObject noticeJ = new JSONObject();
        if (StringUtils.isNotBlank(notice)) {
            noticeJ.put("content", notice);
            noticeJ.put("userId", userId);
            noticeJ.put("noticeTime", DateUtil.now());
            noticeContent = noticeJ.toJSONString();
        }
        LambdaUpdateWrapper<ChatGroup> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatGroup::getId, groupId)
                .set(ChatGroup::getNotice, noticeContent);
        return this.update(updateWrapper);
    }

    @Override
    public Boolean removeGroupMember(Integer groupId, List<Integer> userIds) {
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        ChatGroupInfoDto chatGroupInfo = getChatGroupInfoDto(groupId, chatUser);
        List<Integer> memberIds = chatGroupMemberService.getChatGroupMemberIdListByGroupId(groupId, userIds);
        if (memberIds.size() < userIds.size()) {
            List<Integer> subtract = CollUtil.subtractToList(userIds, memberIds);
            log.error("移除群成员失败,部分用户不属于该群,groupId:{},subtractIds:{}", groupId, subtract);
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "移除群成员失败,部分用户不属于该群");
        }
        return transactionTemplate.execute(t -> {
            boolean ret = removeGroupMember2(groupId, userIds);
            if (!ret) {
                log.error("移除群成员失败,chatUserId:{},groupId:{},userIds:{}", chatUser.getId(), groupId, userIds);
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "移除群成员失败");
            }
            String groupAvatar = getGroupAvatar(groupId, chatUser);
            updateGroupMemberCount(groupId, userIds, chatGroupInfo, groupAvatar);
            List<ChatUserSimDto> userSimDtoList = chatUserService.getChatUserLeftFriendSimList(userIds, chatUser.getId());
            String nicknameStr = userSimDtoList.stream().map(ChatUserSimDto::getOlnickname).collect(Collectors.joining("、"));
            eventPublisher.publishEvent(new ChatGroupMemberRemoveEvent(groupId, userIds, nicknameStr));
            return true;
        });
    }

    private void updateGroupMemberCount(Integer groupId, List<Integer> userIds, ChatGroupInfoDto chatGroupInfo, String groupAvatar) {
        LambdaUpdateWrapper<ChatGroup> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatGroup::getId, groupId)
                .set(ChatGroup::getMemberCount, chatGroupInfo.getMemberCount() - userIds.size());
        setManagersWhere(userIds, chatGroupInfo, updateWrapper);
        updateWrapper.set(ChatGroup::getAvatar, groupAvatar);
        boolean updated = this.update(updateWrapper);
        if (!updated) {
            log.error("更新群成员数失败,userIds:{},groupId:{}", userIds, groupId);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "更新群成员数失败");
        }
    }

    private void setManagersWhere(List<Integer> userIds, ChatGroupInfoDto chatGroupInfo, LambdaUpdateWrapper<ChatGroup> updateWrapper) {
        if (StringUtils.isNotBlank(chatGroupInfo.getManagers())) {
            String[] managers = StringUtils.split(chatGroupInfo.getManagers(), ",");
            ArrayList<String> managerList = CollUtil.toList(managers);
            int managerCount = managerList.size();
            userIds.forEach(userId -> {
                if (CollUtil.contains(managerList, userId.toString())) {
                    managerList.remove(userId.toString());
                }
            });
            if (managerCount < managerList.size()) {
                if (CollUtil.isNotEmpty(managerList)) {
                    updateWrapper.set(ChatGroup::getManagers, CollUtil.join(managerList, ","));
                } else {
                    updateWrapper.set(ChatGroup::getManagers, null);
                }
            }
        }
    }

    private boolean removeGroupMember2(Integer groupId, List<Integer> userIds) {
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatGroupMember::getGroupId, groupId);
        queryWrapper.in(ChatGroupMember::getUserId, userIds);
        return chatGroupMemberService.remove(queryWrapper);
    }

    @Override
    public Boolean logoutGroup(Integer groupId) {
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        ChatGroupInfoDto chatGroupInfo = chatGroupMemberService.getChatGroupInfo(groupId, chatUser.getId());
        if (chatGroupInfo == null) {
            log.error("群信息不存在,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "群信息不存在");
        }
        List<Integer> userIdList = CollUtil.newArrayList(chatUser.getId());
        return transactionTemplate.execute(t -> {
            boolean ret = removeGroupMember(groupId, userIdList);
            if (!ret) {
                log.error("移除群成员失败,chatUserId:{},groupId:{},userId:{}", chatUser.getId(), groupId, chatUser.getId());
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "移除群成员失败");
            }
            String groupAvatar = getGroupAvatar(groupId, chatUser);
            updateGroupMemberCount(groupId, userIdList, chatGroupInfo, groupAvatar);
            String nickname = StringUtils.isBlank(chatGroupInfo.getNickname()) ? chatUser.getNickname() : chatGroupInfo.getNickname();
            eventPublisher.publishEvent(new ChatGroupLogoutEvent(groupId, chatUser.getId(), nickname));
            return true;
        });
    }

    @Override
    public Boolean addGroupMember(Integer groupId, List<Integer> userIds, GroupSourceEm source) {
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        ChatGroupInfoDto chatGroupInfo = chatGroupMemberService.getChatGroupInfo(groupId, chatUser.getId());
        if (ObjectUtil.isNull(chatGroupInfo)) {
            log.error("非本群用户,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "非本群用户,不能添加群成员");
        }
        if (existsGroupMember(groupId, userIds)) {
            log.error("群成员已存在,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            throw new ApiException(ExceptionCodeEm.DATA_ALREADY, "群成员已存在");
        }
        if (!chatGroupInfo.getInviteCfm() || (chatGroupInfo.getIsGroupLeader() || chatGroupInfo.getIsGroupManager())) {
            log.info("群主或管理员添加群成员,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            return addGroupMember(GroupSourceEm.INVITE.name(), userIds, chatUser, chatGroupInfo);
        } else {
            List<Integer> managerList = getManagerList(chatGroupInfo);
            log.info("群成员邀请进群,等待管理员确认,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            return inviteInGroup(groupId, userIds, source, chatUser, managerList);
        }
    }

    private List<Integer> getManagerList(ChatGroupInfoDto chatGroupInfo) {
        List<Integer> managerList = CommUtil.stringToArrayInt(chatGroupInfo.getManagers());
        managerList.add(chatGroupInfo.getGroupLeaderId());
        return managerList;
    }

    private Boolean existsGroupMember(Integer groupId, List<Integer> userIds) {
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatGroupMember::getId);
        queryWrapper.eq(ChatGroupMember::getGroupId, groupId);
        queryWrapper.in(ChatGroupMember::getUserId, userIds);
        queryWrapper.last("limit 1");
        ChatGroupMember one = chatGroupMemberService.getOne(queryWrapper);
        return one != null;
    }

    private Boolean addGroupMember(String source, List<Integer> userIds, ChatUserResponse chatUser, ChatGroupInfoDto chatGroupInfo) {
        Integer groupId = chatGroupInfo.getId();
        List<ChatGroupMember> groupMemberList = getNewChatGroupMembers(groupId, source, chatUser.getId(), userIds);
        return transactionTemplate.execute(t -> {
            boolean ret = chatGroupMemberService.saveBatch(groupMemberList);
            if (!ret) {
                log.error("添加群成员失败,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "添加群成员失败");
            }
            String groupAvatar = getGroupAvatar(groupId, chatUser);
            updateGroupMemberCount(groupId, chatGroupInfo.getMemberCount() + userIds.size(), groupAvatar);
            List<ChatUserSimDto> userSimDtoList = chatUserService.getChatUserLeftFriendSimList(userIds, chatUser.getId());
            String nicknameStr = CollUtil.join(userSimDtoList, "、", ChatUserSimDto::getNickname);
            eventPublisher.publishEvent(new ChatGroupMemberAddEvent(groupId, chatUser.getId(), chatGroupInfo.getNickname(), nicknameStr, userIds));
            return true;
        });
    }

    private String getGroupAvatar(Integer groupId, ChatUserResponse chatUser) {
        List<String> groupMemberAvaterList = chatGroupMemberService.getGroupMemberAvaterList(groupId);
        return generateHeadAndUpload(groupMemberAvaterList, chatUser.getId());
    }

    @Override
    public Boolean applyGroup(Integer groupId, GroupSourceEm source) {
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        ChatGroup chatGroup = this.getById(groupId);
        if (ObjectUtil.isNull(chatGroup)) {
            log.error("群信息不存在,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "非本群用户,不能添加群成员");
        }
        if (existsGroupMember(groupId, CollUtil.newArrayList(chatUser.getId()))) {
            log.error("已经入群,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            throw new ApiException(ExceptionCodeEm.DATA_ALREADY, "已经入群");
        }
        if (!chatGroup.getInviteCfm()) {
            log.info("群主或管理员添加群成员,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            ChatGroupInfoDto chatGroupInfo = new ChatGroupInfoDto();
            chatGroupInfo.setId(groupId);
            chatGroupInfo.setMemberCount(chatGroup.getMemberCount());
            String sourceStr = source.getDesc();
            return addGroupMember(sourceStr, CollUtil.newArrayList(chatUser.getId()), chatUser, chatGroupInfo);
        } else {
            List<Integer> managerList = CommUtil.stringToArrayInt(chatGroup.getManagers());
            managerList.add(chatGroup.getGroupLeaderId());
            log.info("群成员邀请进群,等待管理员确认,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            return inviteInGroup(groupId, CollUtil.newArrayList(chatUser.getId()), source, chatUser, managerList);
        }
    }

    private Boolean inviteInGroup(Integer groupId, List<Integer> userIds, GroupSourceEm source, ChatUserResponse chatUser, List<Integer> managerList) {
        if (existsGroupApply(groupId, userIds)) {
            log.error("已经申请入群,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            throw new ApiException(ExceptionCodeEm.DATA_ALREADY, "已经申请入群");
        }
        ChatGroupApply groupApply = getChatGroupApply(groupId, userIds, source, chatUser.getId());
        return transactionTemplate.execute(t -> {
            boolean saved = chatGroupApplyService.save(groupApply);
            if (!saved) {
                log.error("邀请进群失败,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "邀请进群失败");
            }
            List<ChatGroupApplyUserRel> groupApplyUserRelList = getNewChatGroupApplyUserRelList(groupApply, managerList);
            saveChatGroupApplyRel(groupId, groupApplyUserRelList, chatUser);
            eventPublisher.publishEvent(new ChatGroupApplyEvent(groupId, chatUser.getId(), groupApply.getId(), managerList));
            return true;
        });
    }

    private ChatGroupApply getChatGroupApply(Integer groupId, List<Integer> userIds, GroupSourceEm source, Integer inviteUserId) {
        ChatGroupApply groupApply = new ChatGroupApply();
        groupApply.setGroupId(groupId);
        groupApply.setInviteUserId((source == GroupSourceEm.SEARCH || source == GroupSourceEm.CARD) ? null : inviteUserId);
        groupApply.setApplyUserIds(CollUtil.join(userIds, ","));
        groupApply.setStatus(0);
        groupApply.setSource(source.name());
        return groupApply;
    }

    private Boolean existsGroupApply(Integer groupId, List<Integer> userIds) {
        LambdaQueryWrapper<ChatGroupApply> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatGroupApply::getId);
        queryWrapper.eq(ChatGroupApply::getGroupId, groupId);
        queryWrapper.apply(CommUtil.getFindInSetSql("apply_user_ids", userIds));
        queryWrapper.last("limit 1");
        ChatGroupApply one = chatGroupApplyService.getOne(queryWrapper);
        return one != null;
    }

    private List<ChatGroupApplyUserRel> getNewChatGroupApplyUserRelList(ChatGroupApply groupApply, List<Integer> managerList) {
        List<ChatGroupApplyUserRel> groupApplyUserRelList = new ArrayList<>();
        managerList.forEach(managerId -> {
            ChatGroupApplyUserRel groupApplyUserRel = new ChatGroupApplyUserRel();
            groupApplyUserRel.setApplyId(groupApply.getId());
            groupApplyUserRel.setUserId(managerId);
            groupApplyUserRelList.add(groupApplyUserRel);
        });
        return groupApplyUserRelList;
    }

    private void saveChatGroupApplyRel(Integer groupId, List<ChatGroupApplyUserRel> groupApplyUserRelList, ChatUserResponse chatUser) {
        boolean saved2 = chatGroupApplyUserRelService.saveBatch(groupApplyUserRelList);
        if (!saved2) {
            log.error("邀请进群失败,chatUserId:{},groupId:{},groupApplyUserRelList:{}", chatUser.getId(), groupId, groupApplyUserRelList);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "邀请进群失败");
        }
    }

    private void updateGroupMemberCount(Integer groupId, int memberCount, String groupAvatar) {
        LambdaUpdateWrapper<ChatGroup> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatGroup::getId, groupId);
        updateWrapper.set(ChatGroup::getMemberCount, memberCount);
        updateWrapper.set(ChatGroup::getAvatar, groupAvatar);
        boolean updated = this.update(updateWrapper);
        if (!updated) {
            log.error("更新群成员数失败,groupId:{}", groupId);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "更新群成员数失败");
        }
    }

    private List<ChatGroupMember> getNewChatGroupMembers(Integer groupId, String source, Integer inviteUserId, List<Integer> userIds) {
        List<ChatGroupMember> groupMemberList = new ArrayList<>();
        userIds.forEach(userId -> {
            ChatGroupMember groupMember = new ChatGroupMember();
            groupMember.setGroupId(groupId);
            groupMember.setInviteUserId(inviteUserId);
            groupMember.setUserId(userId);
            groupMember.setSource(source);
            groupMemberList.add(groupMember);
        });
        return groupMemberList;
    }

    @Override
    public Boolean agreeGroupApply(Integer applyId) {
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        ChatGroupApply groupApply = chatGroupApplyService.getById(applyId);
        if (ObjectUtil.isNull(groupApply)) {
            log.error("入群申请信息不存在,chatUserId:{},groupId:{}", chatUser.getId(), groupApply.getGroupId());
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "入群申请信息不存在");
        }
        ChatGroupInfoDto chatGroupInfo = getChatGroupInfoDto(groupApply.getGroupId(), chatUser);
        transactionTemplate.execute(t -> {
            updateGroupApplyStatus(applyId, chatUser, groupApply);
            addGroupMember(groupApply.getSource(), CommUtil.stringToArrayInt(groupApply.getApplyUserIds()), chatUser, chatGroupInfo);
            return true;
        });

        return null;
    }

    private void updateGroupApplyStatus(Integer applyId, ChatUserResponse chatUser, ChatGroupApply groupApply) {
        LambdaUpdateWrapper<ChatGroupApply> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatGroupApply::getId, applyId);
        updateWrapper.set(ChatGroupApply::getStatus, 1);
        updateWrapper.set(ChatGroupApply::getApproveUserId, chatUser.getId());
        boolean updated = chatGroupApplyService.update(updateWrapper);
        if (!updated) {
            log.error("同意入群失败,chatUserId:{},groupId:{}", chatUser.getId(), groupApply.getGroupId());
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "同意入群失败");
        }
    }

    @Override
    public String getGroupQrcode(Integer groupId) {
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        try {
            ClassPathResource resource = new ClassPathResource(chatConfig.getIcon());
            String url = chatConfig.getGroup().getQrcodeUrl().replace("{groupId}", groupId.toString()).replace("{inviteUserId}", chatUser.getId().toString());
            String qrcode = QrCodeBuilder.generateQRCodeBase64(url, 256, resource.getInputStream());
            // 缓存24小时
            String key = CommConstant.CHAT_GROUP_QRCODE_KEY + groupId + ":" + chatUser.getId();
            redisUtil.set(key, qrcode, 60 * 60 * 24L);
            return qrcode;
        } catch (IOException e) {
            log.error("生成二维码失败,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            log.error("生成二维码异常", e);
            throw new ApiException(e);
        }
    }

    @Override
    public Boolean updateInviteCfm(Integer groupId, Boolean flag) {
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        ChatGroupInfoDto chatGroupInfo = getChatGroupInfoDto(groupId, chatUser);
        if (chatGroupInfo.getInviteCfm().equals(flag)) {
            log.error("邀请进群确认状态未改变,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "邀请进群确认状态未改变");
        }
        LambdaUpdateWrapper<ChatGroup> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatGroup::getId, groupId);
        updateWrapper.set(ChatGroup::getInviteCfm, flag);
        return this.update(updateWrapper);
    }

    private ChatGroupInfoDto getChatGroupInfoDto(Integer groupId, ChatUserResponse chatUser) {
        ChatGroupInfoDto chatGroupInfo = chatGroupMemberService.getChatGroupInfo(groupId, chatUser.getId());
        if (chatGroupInfo == null) {
            log.error("群信息不存在,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "群信息不存在");
        }
        if (!chatGroupInfo.getIsGroupLeader() && !chatGroupInfo.getIsGroupManager()) {
            log.error("只有群主或管理员才能更新,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            throw new ApiException(ExceptionCodeEm.FORBIDDEN, "只有群主或管理员才能操作");
        }
        return chatGroupInfo;
    }

    @Override
    public Boolean groupLeaderTransfer(Integer groupId, Integer userId) {
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        if (chatUser.getId().equals(userId)) {
            log.error("不能转让给自己,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "不能转让给自己");
        }

        return transactionTemplate.execute(t -> {
            checkGroupInfo(groupId, chatUser);
            if (!existsGroupMember(groupId, CollUtil.newArrayList(userId))) {
                log.error("群成员不存在,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
                throw new ApiException(ExceptionCodeEm.NOT_FOUND, "群成员不存在");
            }
            boolean updated = updateGroupLeader(groupId, CollUtil.newArrayList(chatUser.getId(), userId));
            if (!updated) {
                log.error("群主转让失败,chatUserId:{},groupId:{},userId:{}", chatUser.getId(), groupId, userId);
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "群主转让失败");
            }
            ChatGroup chatGroup = this.getById(groupId);
            List<Integer> managerIds = CommUtil.stringToArrayInt(chatGroup.getManagers());
            // 更新群管理员
            transferUpdateGroupManagers(groupId, userId, managerIds, chatUser);
            return true;
        });
    }

    private void transferUpdateGroupManagers(Integer groupId, Integer userId, List<Integer> managerIds, ChatUserResponse chatUser) {
        LambdaUpdateWrapper<ChatGroup> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatGroup::getId, groupId);
        updateWrapper.set(ChatGroup::getGroupLeaderId, userId);
        if (managerIds.contains(userId)) {
            managerIds.remove(userId);
            updateWrapper.set(ChatGroup::getManagers, CollUtil.join(managerIds, ","));
        }
        if (!this.update(updateWrapper)) {
            log.error("移除群管理员失败,chatUserId:{},groupId:{},userId:{}", chatUser.getId(), groupId, userId);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "移除群管理员失败");
        }
    }

    private boolean updateGroupLeader(Integer groupId, List<Integer> userIds) {
        LambdaUpdateWrapper<ChatGroupMember> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatGroupMember::getGroupId, groupId);
        updateWrapper.in(ChatGroupMember::getUserId, userIds);
        updateWrapper.setSql("is_group_leader = !is_group_leader");
        updateWrapper.set(ChatGroupMember::getIsGroupManager, false);
        return chatGroupMemberService.update(updateWrapper);
    }

    @Override
    public Boolean dissolveGroup(Integer groupId) {
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        checkGroupInfo(groupId, chatUser);
        LambdaUpdateWrapper<ChatGroup> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatGroup::getId, groupId);
        updateWrapper.set(ChatGroup::getIsDissolve, true);
        updateWrapper.set(ChatGroup::getDissolveTime, DateUtil.now());
        return this.update(updateWrapper);
    }

    @Override
    public Boolean removeGroupManager(Integer groupId, Integer userId) {
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        checkGroupInfo(groupId, chatUser);
        return transactionTemplate.execute(t -> {
            ChatGroup chatGroup = this.getById(groupId);
            if (chatGroup == null) {
                log.error("群信息不存在,chatUserId:{},groupId:{},userId:{}", chatUser.getId(), groupId, userId);
                throw new ApiException(ExceptionCodeEm.NOT_FOUND, "群信息不存在");
            }
            List<Integer> managerIds = CommUtil.stringToArrayInt(chatGroup.getManagers());
            if (!managerIds.contains(userId)) {
                log.error("群成员不是管理员,chatUserId:{},groupId:{},userId:{}", chatUser.getId(), groupId, userId);
                throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "群成员不是管理员");
            }
            // 更新群成员管理员标志
            updateGroupManagerFlag(groupId, userId, false);
            // 更新群管理员
            updateGroupManagers(groupId, userId, managerIds, chatUser);

            return true;
        });
    }

    private void updateGroupManagers(Integer groupId, Integer userId, List<Integer> managerIds, ChatUserResponse chatUser) {
        managerIds.remove(userId);
        LambdaUpdateWrapper<ChatGroup> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatGroup::getId, groupId);
        if (managerIds.isEmpty()) {
            updateWrapper.set(ChatGroup::getManagers, null);
        } else {
            updateWrapper.set(ChatGroup::getManagers, CollUtil.join(managerIds, ","));
        }
        if (!this.update(updateWrapper)) {
            log.error("移除群管理员失败,chatUserId:{},groupId:{},userId:{}", chatUser.getId(), groupId, userId);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "移除群管理员失败");
        }
    }

    @Override
    public Boolean addGroupManager(Integer groupId, Integer userId) {
        ChatUserResponse chatUser = chatUserService.getCurrentChatUser();
        ChatGroup chatGroup = checkGroupInfo(groupId, chatUser);
        if (!existsGroupMember(groupId, CollUtil.newArrayList(userId))) {
            log.error("群成员不存在,chatUserId:{},groupId:{},userId:{}", chatUser.getId(), groupId, userId);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "群成员不存在");
        }
        String managers = getManagerIds(groupId, userId, chatGroup.getManagers());
        return transactionTemplate.execute(t -> {
            // 更新群成员管理员标志
            updateGroupManagerFlag(groupId, userId, true);
            // 更新群管理员
            updateGroupManagerIds(groupId, userId, managers);

            return true;
        });
    }

    private void updateGroupManagerFlag(Integer groupId, Integer userId, boolean isGroupLeader) {
        LambdaUpdateWrapper<ChatGroupMember> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatGroupMember::getGroupId, groupId);
        updateWrapper.eq(ChatGroupMember::getUserId, userId);
        updateWrapper.set(ChatGroupMember::getIsGroupManager, isGroupLeader);
        boolean updated = chatGroupMemberService.update(updateWrapper);
        if (!updated) {
            log.error("更新群管理员标志失败,groupId:{},userId:{}", groupId, userId);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "更新群管理员标志失败");
        }
    }

    private void updateGroupManagerIds(Integer groupId, Integer userId, String managers) {
        LambdaUpdateWrapper<ChatGroup> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatGroup::getId, groupId);
        updateWrapper.set(ChatGroup::getManagers, managers);
        if (!this.update(updateWrapper)) {
            log.error("添加群管理员失败,groupId:{},userId:{}", groupId, userId);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "添加群管理员失败");
        }
    }

    private String getManagerIds(Integer groupId, Integer userId, String managers) {
        List<Integer> managerIdList = new ArrayList<>();
        managerIdList.add(userId);
        if (StringUtils.isNotBlank(managers)) {
            List<Integer> managerIds = CommUtil.stringToArrayInt(managers);
            managerIdList.addAll(managerIds);
            managerIdList = managerIdList.stream().distinct().collect(Collectors.toList());
            if (managerIdList.size() > 3) {
                log.error("群管理员已达到上限,groupId:{},userId:{}", groupId, userId);
                throw new ApiException(ExceptionCodeEm.DATA_ALREADY, "群管理员已达到上限");
            }
        }
        return CollUtil.join(managerIdList, ",");
    }

    private ChatGroup checkGroupInfo(Integer groupId, ChatUserResponse chatUser) {
        ChatGroup chatGroup = this.getById(groupId);
        if (chatGroup == null) {
            log.error("群信息不存在,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "群信息不存在");
        }
        if (!chatGroup.getGroupLeaderId().equals(chatUser.getId())) {
            log.error("只有群主才能操作,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            throw new ApiException(ExceptionCodeEm.FORBIDDEN, "只有群主才能操作");
        }
        if (chatGroup.getIsDissolve()) {
            log.error("群已解散,chatUserId:{},groupId:{}", chatUser.getId(), groupId);
            throw new ApiException(ExceptionCodeEm.FORBIDDEN, "群已解散");
        }
        return chatGroup;
    }


}
