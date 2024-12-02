package com.dot.msg.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.comm.constants.CommConstant;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.entity.LoginUsername;
import com.dot.comm.exception.ApiException;
import com.dot.comm.manager.TokenManager;
import com.dot.comm.utils.AESUtil;
import com.dot.comm.utils.CommUtil;
import com.dot.msg.chat.dao.ChatFriendDao;
import com.dot.msg.chat.dao.ChatUserDao;
import com.dot.msg.chat.dto.ChatUserFriendDto;
import com.dot.msg.chat.dto.ChatUserSimDto;
import com.dot.msg.chat.em.ChatSourceEm;
import com.dot.msg.chat.model.ChatFriend;
import com.dot.msg.chat.model.ChatUser;
import com.dot.msg.chat.response.ChatUserInfoResponse;
import com.dot.msg.chat.response.ChatUserResponse;
import com.dot.msg.chat.response.ChatUserSearchResponse;
import com.dot.msg.chat.service.ChatUserService;
import com.dot.sys.upload.response.UploadResponse;
import com.dot.sys.upload.service.UploadService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 聊天室用户表(关联管理员表和企业用户表)服务接口实现
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Slf4j
@Service
public class ChatUserServiceImpl extends ServiceImpl<ChatUserDao, ChatUser> implements ChatUserService {

    @Resource
    private ChatFriendDao chatFriendDao;

    @Resource
    private UploadService uploadService;

    @Resource
    private TokenManager tokenManager;

    private final Lock lock = new ReentrantLock();

    @Override
    public List<ChatUser> getList(List<Integer> userIds) {
        if (ObjectUtil.isEmpty(userIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ChatUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(ChatUser::getId, userIds);
        return this.list(queryWrapper);
    }

    @Override
    public List<ChatUser> getSimList(List<Integer> userIds) {
        if (ObjectUtil.isEmpty(userIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ChatUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatUser::getId, ChatUser::getNickname, ChatUser::getAvatar, ChatUser::getIsOnline);
        queryWrapper.in(ChatUser::getId, userIds);
        return this.list(queryWrapper);
    }

    @Override
    public List<ChatUserSimDto> getChatUserLeftFriendSimList(List<Integer> userIds, Integer currentUserId) {
        if (ObjectUtil.isEmpty(userIds)) {
            return new ArrayList<>();
        }
        return baseMapper.selectChatUserLeftFriendSimList(CollUtil.join(userIds, ","), currentUserId);
    }

    @Override
    public List<Integer> getAllChatUserIds() {
        LambdaQueryWrapper<ChatUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatUser::getId);
        return this.listObjs(queryWrapper, obj -> (Integer) obj);
    }

    @Override
    public ChatUser getByPhone(String phone) {
        LambdaQueryWrapper<ChatUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatUser::getPhone, phone);
        return this.getOne(queryWrapper);
    }

    @Override
    public ChatUser getChatUser(Integer userId) {
        LambdaQueryWrapper<ChatUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatUser::getId, userId);
        return this.getOne(queryWrapper);
    }

    @Override
    public Integer getChatUserId(Integer userId) {
        LambdaQueryWrapper<ChatUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatUser::getId);
        queryWrapper.eq(ChatUser::getId, userId);
        ChatUser one = this.getOne(queryWrapper);
        return ObjectUtil.isNotNull(one) ? one.getId() : null;
    }

    @Override
    public ChatUserResponse getCurrentChatUser() {
        LoginUsername loginUser = tokenManager.getLoginUser();
        ChatUser user = this.getChatUser(loginUser.getUid());
        return BeanUtil.copyProperties(user, ChatUserResponse.class);
    }

    @Override
    public Integer getCurrentChatUserId() {
        return tokenManager.getLoginUser().getUid();
    }

    @Override
    public String getNicknameById(Integer id) {
        LambdaQueryWrapper<ChatUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatUser::getNickname);
        queryWrapper.eq(ChatUser::getId, id);
        ChatUser one = this.getOne(queryWrapper);
        if (ObjectUtil.isNotNull(one)) {
            return one.getNickname();
        }
        return null;
    }

    @Override
    public boolean updateOnlineStatus(Integer chatUserId, boolean isOnline) {
        LambdaUpdateWrapper<ChatUser> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatUser::getId, chatUserId)
                .eq(ChatUser::getIsOnline, !isOnline)
                .set(ChatUser::getIsOnline, isOnline);
        return this.update(updateWrapper);
    }

    @Override
    public List<ChatUserSearchResponse> getSearchChatUserList(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return new ArrayList<>(0);
        }
        List<Integer> friendIds = getFriendIds();
        LambdaQueryWrapper<ChatUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatUser::getId, ChatUser::getNickname, ChatUser::getAvatar,ChatUser::getSex,ChatUser::getSignature);
        queryWrapper.notIn(ChatUser::getId, friendIds);
        queryWrapper.and(wrapper -> {
            wrapper.like(ChatUser::getNickname, keyword);
            wrapper.or().like(StringUtils.isNumeric(keyword), ChatUser::getPhone, keyword);
        });
        queryWrapper.last("limit 10");
        List<ChatUser> chatUserList = this.list(queryWrapper);
        return BeanUtil.copyToList(chatUserList, ChatUserSearchResponse.class);
    }

    private List<Integer> getFriendIds() {
        Integer chatUserId = this.getCurrentChatUserId();
        List<Integer> friendIds = getFriendIds(chatUserId);
        friendIds.add(chatUserId);
        return friendIds;
    }

    private List<Integer> getFriendIds(Integer userId) {
        LambdaQueryWrapper<ChatFriend> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatFriend::getUserId, userId).select(ChatFriend::getFriendId);
        return chatFriendDao.selectObjs(queryWrapper);
    }

    @Override
    public ChatUserInfoResponse getChatUserInfo(Integer userId) {
        ChatUserResponse chatUser = this.getCurrentChatUser();
        ChatUserInfoResponse response = new ChatUserInfoResponse();
        if (chatUser.getId().equals(userId)) {
            response.setIsFriend(true);
            BeanUtil.copyProperties(chatUser, response);
            response.setUserId(userId);
            return response;
        }
        ChatUserFriendDto chatUserFriendDto = chatFriendDao.selectChatUserFriendInfo(chatUser.getId(), userId);
        if (ObjectUtil.isNotNull(chatUserFriendDto)) {
            BeanUtil.copyProperties(chatUserFriendDto, response);
            response.setIsFriend(true);
            response.setUserId(userId);
            response.setSourceDesc(ChatSourceEm.getDesc(chatUserFriendDto.getSource()));
        } else {
            log.warn("用户非好友,chatUserId:{},userId:{}", chatUser.getId(), userId);
            ChatUser user = getById(userId);
            if (ObjectUtil.isNull(user)) {
                log.error("用户不存在,userId:{}", userId);
                throw new ApiException(ExceptionCodeEm.NOT_FOUND, "用户不存在");
            }
            BeanUtil.copyProperties(user, response);
            response.setIsFriend(false);
            response.setUserId(userId);
        }
        return response;
    }

    @Override
    public List<Integer> getAllUserIds() {
        LambdaQueryWrapper<ChatUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatUser::getId);
        List<ChatUser> chatUserList = this.list(queryWrapper);
        return chatUserList.stream().map(ChatUser::getId).collect(Collectors.toList());
    }

    @Override
    public String updateAvatar(MultipartFile imgFile) {
        if (ObjectUtil.isNull(imgFile)) {
            log.error("上传头像失败,imgFile为空");
            throw new ApiException(ExceptionCodeEm.PRAM_NOT_MATCH, "请选择头像文件");
        }
        Integer chatUserId = getCurrentChatUserId();
        UploadResponse uploadResponse = uploadService.uploadImage(imgFile, "chat-msg");

        LambdaUpdateWrapper<ChatUser> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatUser::getId, chatUserId);
        updateWrapper.set(ChatUser::getAvatar, uploadResponse.getUrl());
        if (this.update(updateWrapper)) {
            return uploadResponse.getUrl();
        }
        log.error("更新头像失败,chatUserId:{}", chatUserId);
        throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "上传头像失败");
    }

    @Override
    public Boolean updateNickname(String nickname, Integer sex,String signature) {
        Integer chatUserId = getCurrentChatUserId();
        LambdaUpdateWrapper<ChatUser> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatUser::getId, chatUserId);
        updateWrapper.set(ChatUser::getNickname, nickname);
        updateWrapper.set(ChatUser::getSex, sex);
        updateWrapper.set(ChatUser::getSignature, signature);
        return this.update(updateWrapper);
    }

    @Override
    public void updateLastLoginIpTime(Integer uid) {
        LambdaUpdateWrapper<ChatUser> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatUser::getId, uid);
        updateWrapper.set(ChatUser::getLastLoginTime, DateUtil.now());
        updateWrapper.set(ChatUser::getLastIp, CommUtil.getClientIp());
        this.update(updateWrapper);
    }

    @Override
    public Boolean updatePassword(Integer uid, String oldPwd, String newPwd) {
        ChatUser user = getById(uid);
        checkPwd(user.getPwd(), oldPwd, newPwd, user.getPhone());
        LambdaUpdateWrapper<ChatUser> lqw = Wrappers.lambdaUpdate();
        lqw.eq(ChatUser::getId, uid);
        lqw.set(ChatUser::getPwd, AESUtil.encryptCBC(user.getPhone(), newPwd));
        return this.update(lqw);
    }

    private void checkPwd(String pwd, String oldPwd, String newPwd, String account) {
        if (!pwd.equals(CommUtil.encryptPassword(oldPwd, account))) {
            log.error("原密码错误,account:{}", account);
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "原密码错误");
        }
        if (pwd.equals(CommUtil.encryptPassword(newPwd, account))) {
            log.error("新密码不能与原密码相同,account:{}", account);
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "新密码不能与原密码相同");
        }
    }

    @Override
    public boolean isExist(String phone) {
        LambdaQueryWrapper<ChatUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatUser::getId);
        queryWrapper.eq(ChatUser::getPhone, phone);
        queryWrapper.last("limit 1");
        return ObjectUtil.isNotNull(this.getOne(queryWrapper));
    }

    @Override
    public ChatUser addNewUser(String phone, String password, String nickname) {
        if (isExist(phone)) {
            log.error("账号已存在,phone:{}", phone);
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "账号已存在");
        }
        ChatUser newUser = new ChatUser();
        newUser.setNickname(StringUtils.isBlank(nickname) ? phone : nickname);
        newUser.setPhone(phone);
        newUser.setPwd(AESUtil.encryptCBC(phone, password));
        newUser.setAvatar(CommConstant.DEFAULT_AVATAR);
        boolean saved = this.save(newUser);
        if (!saved) {
            log.error("注册失败,newUser:{}", newUser);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "注册失败");
        }
        return newUser;
    }
}
