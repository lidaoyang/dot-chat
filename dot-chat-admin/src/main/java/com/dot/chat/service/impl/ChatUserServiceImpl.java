package com.dot.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.chat.dao.ChatUserDao;
import com.dot.chat.dto.ChatUserRegisterCountDto;
import com.dot.chat.model.ChatUser;
import com.dot.chat.request.ChatUserSearchRequest;
import com.dot.chat.response.ChatUserRegisterCountResponse;
import com.dot.chat.response.ChatUserResponse;
import com.dot.chat.response.ChatUserSimResponse;
import com.dot.chat.service.ChatUserService;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.entity.PageParam;
import com.dot.comm.exception.ApiException;
import com.dot.comm.utils.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天室用户表(关联管理员表和企业用户表)服务接口实现
 *
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Slf4j
@Service
public class ChatUserServiceImpl extends ServiceImpl<ChatUserDao, ChatUser> implements ChatUserService {

    @Override
    public IPage<ChatUserResponse> getList(ChatUserSearchRequest request, PageParam pageParam) {
        LambdaQueryWrapper<ChatUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ObjectUtil.isNotNull(request.getStatus()), ChatUser::getStatus, request.getStatus());
        queryWrapper.eq(ObjectUtil.isNotNull(request.getIsOnline()), ChatUser::getIsOnline, request.getIsOnline());
        queryWrapper.and(StringUtils.isNotBlank(request.getKeywords()), wrapper -> {
            wrapper.eq(NumberUtil.isNumber(request.getKeywords()), ChatUser::getId, request.getKeywords())
                    .or().like(ChatUser::getNickname, request.getKeywords())
                    .or().like(ChatUser::getPhone, request.getKeywords());
        });
        queryWrapper.orderByDesc(ChatUser::getLastLoginTime).orderByDesc(ChatUser::getId);
        IPage<ChatUser> page = this.page(Page.of(pageParam.getPageIndex(), pageParam.getPageSize()), queryWrapper);
        if (page.getRecords().isEmpty()) {
            return PageUtil.copyPage(page, new ArrayList<>());
        }
        List<ChatUserResponse> responseList = BeanUtil.copyToList(page.getRecords(), ChatUserResponse.class);
        return PageUtil.copyPage(page, responseList);
    }

    @Override
    public Boolean updateUserStatus(Integer id, Boolean status) {
        ChatUser chatUser = this.getById(id);
        if (ObjectUtil.isNull(chatUser)) {
            log.error("用户不存在,id:{}", id);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "用户不存在");
        }
        LambdaUpdateWrapper<ChatUser> lqw = Wrappers.lambdaUpdate();
        lqw.eq(ChatUser::getId, id);
        lqw.set(ChatUser::getStatus, status);
        return this.update(lqw);
    }

    @Override
    public List<ChatUserRegisterCountResponse> getUserRegisterCount() {
        List<ChatUserRegisterCountDto> countDtoList = baseMapper.selectUserRegisterCount();
        return BeanUtil.copyToList(countDtoList, ChatUserRegisterCountResponse.class);
    }

    @Override
    public List<ChatUserSimResponse> getUserSimList(String date) {
        LambdaQueryWrapper<ChatUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(StringUtils.isNotBlank(date), ChatUser::getCreateDate, date);
        queryWrapper.orderByDesc(ChatUser::getId);
        List<ChatUser> chatUserList = this.list(queryWrapper);
        return BeanUtil.copyToList(chatUserList, ChatUserSimResponse.class);
    }
}
