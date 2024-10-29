package com.dot.msg.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.comm.constants.CommConstant;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.em.UserTypeEm;
import com.dot.comm.entity.LoginUsername;
import com.dot.comm.exception.ApiException;
import com.dot.comm.manager.TokenManager;
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
import com.dot.sys.user.model.SystemAdmin;
import com.dot.sys.user.model.User;
import com.dot.sys.user.service.EnterpriseService;
import com.dot.sys.user.service.SupplierService;
import com.dot.sys.user.service.SystemAdminService;
import com.dot.sys.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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
    private UserService userService;

    @Resource
    private SystemAdminService systemAdminService;

    @Resource
    private EnterpriseService enterpriseService;

    @Resource
    private SupplierService supplierService;

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
    public ChatUser getChatUser(Integer userId, String userType) {
        LambdaQueryWrapper<ChatUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatUser::getUserId, userId).eq(ChatUser::getUserType, userType);
        return this.getOne(queryWrapper);
    }

    @Override
    public Integer getChatUserId(Integer userId, String userType) {
        LambdaQueryWrapper<ChatUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatUser::getId);
        queryWrapper.eq(ChatUser::getUserId, userId).eq(ChatUser::getUserType, userType);
        ChatUser one = this.getOne(queryWrapper);
        return ObjectUtil.isNotNull(one) ? one.getId() : null;
    }

    @Override
    public ChatUserResponse getCurrentChatUser(UserTypeEm userType) {
        LoginUsername loginUser = tokenManager.getLoginUser(userType);
        ChatUser user = this.getChatUser(loginUser.getUserId(), loginUser.getType());
        if (ObjectUtil.isNull(user)) {
            user = this.addChatUser(loginUser);
        }
        if (!user.getIsOnline()) {
            updateOnlineStatus(user.getId(), true);
            user.setIsOnline(true);
        }
        return BeanUtil.copyProperties(user, ChatUserResponse.class);
    }

    @Override
    public Integer getCurrentChatUserId(UserTypeEm userType) {
        LoginUsername loginUser = tokenManager.getLoginUser(userType);
        Integer userId = this.getChatUserId(loginUser.getUserId(), loginUser.getType());
        if (ObjectUtil.isNull(userId)) {
            userId = this.addChatUser(loginUser).getId();
        }
        return userId;
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
    public ChatUser addChatUser(LoginUsername loginUser) {
        lock.lock();
        try {
            log.info("聊天室用户添加[上锁],loginUser:{}", loginUser.mergeUsername());
            ChatUser chatUser1 = this.getChatUser(loginUser.getUserId(), loginUser.getType());
            if (ObjectUtil.isNotNull(chatUser1)) {
                log.info("聊天室用户添加[已存在]");
                return chatUser1;
            }
            ChatUser chatUser = getNewChatUser(loginUser);
            boolean saved = this.save(chatUser);
            if (saved) {
                log.info("聊天室用户添加成功,loginUser:{}", loginUser.mergeUsername());
                return chatUser;
            }
            log.error("聊天室用户添加失败,chatUser:{}", JSON.toJSONString(chatUser));
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "聊天室用户添加失败");
        }finally {
            lock.unlock();
            log.info("聊天室用户添加[解锁],loginUser:{}", loginUser.mergeUsername());
        }
    }

    private ChatUser getNewChatUser(LoginUsername loginUser) {
        ChatUser chatUser = new ChatUser();
        chatUser.setIsOnline(true);
        chatUser.setUserId(loginUser.getUserId());
        chatUser.setUserType(loginUser.getType());
        chatUser.setEnterpriseId(loginUser.getEnterpriseId());
        chatUser.setPhone(loginUser.getAccount());
        User user = getUser(loginUser);
        String nickname = StringUtils.isBlank(user.getNickname()) ? user.getRealName() : user.getNickname();
        nickname = StringUtils.isBlank(nickname) ? loginUser.getAccount() : nickname;
        chatUser.setNickname(nickname);
        chatUser.setAvatar(user.getAvatar());
        if (StringUtils.isBlank(chatUser.getAvatar())) {
            chatUser.setAvatar(CommConstant.DEFAULT_AVATAR);
        }
        chatUser.setEnterpriseName(getEnterpriseName(loginUser.getType(), loginUser.getEnterpriseId()));
        return chatUser;
    }

    private User getUser(LoginUsername loginUser) {
        User user = null;
        if (loginUser.isEntUser()) {
            LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.select(User::getNickname, User::getRealName, User::getAvatar);
            queryWrapper.eq(User::getUid, loginUser.getUserId());
            user = userService.getOne(queryWrapper);
        } else {
            LambdaQueryWrapper<SystemAdmin> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.select(SystemAdmin::getRealName, SystemAdmin::getAvatar);
            queryWrapper.eq(SystemAdmin::getId, loginUser.getUserId());
            SystemAdmin systemAdmin = systemAdminService.getOne(queryWrapper);
            if (ObjectUtil.isNotNull(systemAdmin)) {
                user = new User();
                user.setRealName(systemAdmin.getRealName());
                user.setAvatar(systemAdmin.getAvatar());
            }
        }
        if (ObjectUtil.isNull(user)) {
            log.error("用户不存在,userId:{}", loginUser.getUserId());
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private String getEnterpriseName(String userType, Integer enterpriseId) {
        String enterpriseName = "";
        if (UserTypeEm.SUPPLIER.getCode().equals(userType)) {
            enterpriseName = supplierService.getNameById(enterpriseId);
        } else {
            enterpriseName = enterpriseService.getNameById(enterpriseId);
        }
        return enterpriseName;
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
    public List<ChatUserSearchResponse> getSearchChatUserList(UserTypeEm userType, String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return new ArrayList<>(0);
        }
        List<Integer> friendIds = getFriendIds(userType);
        LambdaQueryWrapper<ChatUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatUser::getId, ChatUser::getNickname, ChatUser::getAvatar, ChatUser::getEnterpriseName);
        if (userType == UserTypeEm.ENT_USER) {
            queryWrapper.eq(ChatUser::getUserType, UserTypeEm.ENT_USER.getCode());
        } else {
            queryWrapper.ne(ChatUser::getUserType, UserTypeEm.ENT_USER.getCode());
        }
        queryWrapper.notIn(ChatUser::getId, friendIds);
        queryWrapper.and(wrapper -> {
            wrapper.like(ChatUser::getNickname, keyword);
            wrapper.or().like(StringUtils.isNumeric(keyword), ChatUser::getPhone, keyword);
            wrapper.or().like(ChatUser::getEnterpriseName, keyword);
        });
        queryWrapper.last("limit 10");
        List<ChatUser> chatUserList = this.list(queryWrapper);
        return BeanUtil.copyToList(chatUserList, ChatUserSearchResponse.class);
    }

    private List<Integer> getFriendIds(UserTypeEm userType) {
        Integer chatUserId = this.getCurrentChatUserId(userType);
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
    public ChatUserInfoResponse getChatUserInfo(UserTypeEm userType, Integer userId) {
        ChatUserResponse chatUser = this.getCurrentChatUser(userType);
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
    public List<Integer> getChatUserIds(UserTypeEm userType, Integer enterpriseId) {
        LambdaQueryWrapper<ChatUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ChatUser::getId);
        queryWrapper.eq(ChatUser::getUserType, userType.getCode());
        queryWrapper.eq(ChatUser::getEnterpriseId, enterpriseId);
        List<ChatUser> chatUserList = this.list(queryWrapper);
        return chatUserList.stream().map(ChatUser::getId).collect(Collectors.toList());
    }

    @Override
    public boolean updatePhoneAndEnterpriseId(Integer id, String userType, Integer enterpriseId, String account) {
        if (StringUtils.isBlank(account) && ObjectUtil.isNull(enterpriseId)) {
            return false;
        }
        LambdaUpdateWrapper<ChatUser> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatUser::getId, id);
        updateWrapper.set(StringUtils.isNotBlank(account), ChatUser::getPhone, account);
        if (ObjectUtil.isNotNull(enterpriseId)) {
            updateWrapper.set(ChatUser::getEnterpriseId, enterpriseId);
            String enterpriseName = getEnterpriseName(userType, enterpriseId);
            updateWrapper.set(ChatUser::getEnterpriseName, enterpriseName);
        }
        return this.update(updateWrapper);
    }

    @Override
    public String updateAvatar(MultipartFile imgFile, UserTypeEm userType) {
        if (ObjectUtil.isNull(imgFile)) {
            log.error("上传头像失败,imgFile为空");
            throw new ApiException(ExceptionCodeEm.PRAM_NOT_MATCH, "请选择头像文件");
        }
        Integer chatUserId = getCurrentChatUserId(userType);
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
    public Boolean updateNickname(UserTypeEm userType, String nickname, Integer sex) {
        Integer chatUserId = getCurrentChatUserId(userType);
        LambdaUpdateWrapper<ChatUser> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatUser::getId, chatUserId);
        updateWrapper.set(ChatUser::getNickname, nickname);
        updateWrapper.set(ChatUser::getSex, sex);
        return this.update(updateWrapper);
    }
}
