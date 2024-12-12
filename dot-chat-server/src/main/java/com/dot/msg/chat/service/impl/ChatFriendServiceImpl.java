package com.dot.msg.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.comm.constants.CommConstant;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.exception.ApiException;
import com.dot.comm.utils.PinYinUtil;
import com.dot.comm.utils.StrUtils;
import com.dot.msg.chat.dao.ChatFriendDao;
import com.dot.msg.chat.dto.ChatUserFriendDto;
import com.dot.msg.chat.em.ChatSourceEm;
import com.dot.msg.chat.model.ChatFriend;
import com.dot.msg.chat.model.ChatFriendApplyUserRel;
import com.dot.msg.chat.request.ChatFriendAddRequest;
import com.dot.msg.chat.response.ChatUserFriendInfoResponse;
import com.dot.msg.chat.response.ChatUserFriendResponse;
import com.dot.msg.chat.service.ChatFriendApplyUserRelService;
import com.dot.msg.chat.service.ChatFriendService;
import com.dot.msg.chat.service.ChatUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聊天室好友表服务接口实现
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Slf4j
@Service
public class ChatFriendServiceImpl extends ServiceImpl<ChatFriendDao, ChatFriend> implements ChatFriendService {

    @Resource
    private ChatUserService chatUserService;

    @Resource
    private ChatFriendApplyUserRelService chatFriendApplyUserRelService;


    @Override
    public List<ChatUserFriendResponse> getChatUserFriendList( String keywords) {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        return getChatUserFriendList(chatUserId, null, keywords);
    }

    @Override
    public List<ChatUserFriendResponse> getChatUserFriendList(Integer userId, List<Integer> friendIds) {
        return getChatUserFriendList(userId, friendIds, null, null);
    }

    @Override
    public List<ChatUserFriendResponse> getChatUserFriendList( Integer filterGroupId, String keywords) {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        return getChatUserFriendList(chatUserId, filterGroupId, keywords);
    }

    @Override
    public List<ChatUserFriendResponse> getChatUserFriendList(Integer userId, Integer filterGroupId, String keywords) {
        return getChatUserFriendList(userId, null, filterGroupId, keywords);
    }

    @Override
    public List<ChatUserFriendResponse> getChatUserFriendList(Integer userId, List<Integer> friendIds, Integer filterGroupId, String keywords) {
        String andSql = "";
        if (CollUtil.isNotEmpty(friendIds)) {
            andSql = " and cf.friend_id in (" + StringUtils.join(friendIds, ",") + ")";
        }
        if (ObjectUtil.isNotNull(filterGroupId)) {
            andSql = andSql + " and cf.friend_id not in(select gm.user_id from chat_group_member gm where gm.group_id = " + filterGroupId + ") ";
        }
        if (StringUtils.isNotBlank(keywords)) {
            keywords = StrUtils.mysqlSpecialCharEscape(keywords);
            andSql = andSql + " and (cu.phone like '" + keywords + "%' or cf.remark like '%" + keywords + "%' or cu.nickname like '%" + keywords + "%')";
        }
        List<ChatUserFriendDto> chatUserFriendList = baseMapper.selectChatUserFriendList(userId, andSql);
        if (CollUtil.isEmpty(chatUserFriendList)) {
            return Collections.emptyList();
        }
        // 把首字母为“_”替换为“#”
        replaceLastLatter(chatUserFriendList);
        return BeanUtil.copyToList(chatUserFriendList, ChatUserFriendResponse.class);
    }

    private void replaceLastLatter(List<ChatUserFriendDto> chatUserFriendList) {
        chatUserFriendList.forEach(friendDto -> {
            if (friendDto.getInitial().equals(CommConstant.LAST_LATTER)) {
                friendDto.setInitial(CommConstant.REP_LATTER);
            }
            if (StringUtils.isNotBlank(friendDto.getRemark())) {
                friendDto.setNickname(friendDto.getRemark());
            }
        });
    }

    @Override
    public ChatUserFriendInfoResponse getChatUserFriendInfo( Integer friendId) {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        ChatUserFriendDto chatUserFriendDto = baseMapper.selectChatUserFriendInfo(chatUserId, friendId);
        if (ObjectUtil.isNull(chatUserFriendDto)) {
            log.error("好友不存在,chatUserId:{},friendId:{}", chatUserId, friendId);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "好友不存在");
        }
        ChatUserFriendInfoResponse response = BeanUtil.copyProperties(chatUserFriendDto, ChatUserFriendInfoResponse.class);
        response.setSourceDesc(ChatSourceEm.getDesc(chatUserFriendDto.getSource()));
        return response;
    }

    @Override
    public boolean addChatFriend(ChatFriendAddRequest request) {
        List<ChatFriend> chatFriendList = getAddChatFriends(request);
        return this.saveBatch(chatFriendList);
    }

    private List<ChatFriend> getAddChatFriends(ChatFriendAddRequest request) {
        List<ChatFriend> chatFriendList = new ArrayList<>();
        ChatFriend chatFriend = getAddChatFriend(request.getApplyUserId(), request.getFriendId(), request.getRemark(), request.getNickname(), request.getLabel());
        chatFriend.setSource(request.getSource());
        chatFriendList.add(chatFriend);

        ChatFriend chatFriend2 = getAddChatFriend(request.getFriendId(), request.getApplyUserId(), request.getApplyAgreeRequest().getRemark(), request.getApplyAgreeRequest().getNickname(), request.getApplyAgreeRequest().getLabel());
        chatFriend2.setSource(request.getSource());
        chatFriendList.add(chatFriend2);
        return chatFriendList;
    }

    private ChatFriend getAddChatFriend(Integer userId, Integer friendId, String remark, String nickname, String label) {
        ChatFriend chatFriend = new ChatFriend();
        chatFriend.setUserId(userId);
        chatFriend.setFriendId(friendId);
        chatFriend.setRemark(remark);
        chatFriend.setLabel(label);
        chatFriend.setInitial(getInitial(remark, nickname));
        return chatFriend;
    }

    private String getInitial(String remark, String nickname) {
        String nick = StringUtils.isBlank(remark) ? nickname : remark;
        String first = nick.substring(0, 1);
        String firstPY = PinYinUtil.getFirstLetterUpper(first);
        if (!CharUtil.isLetter(firstPY.charAt(0))) {
            firstPY = CommConstant.LAST_LATTER;
        }
        return firstPY;
    }

    @Override
    public List<Integer> getFriendIds(Integer userId) {
        LambdaQueryWrapper<ChatFriend> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatFriend::getUserId, userId).select(ChatFriend::getFriendId);
        return listObjs(queryWrapper);
    }

    @Override
    public String modifyFriendRemark( Integer friendId, String remark) {
        Integer chatUserId = chatUserService.getCurrentChatUserId();
        String friendNickname = chatUserService.getNicknameById(friendId);
        if (StringUtils.isBlank(friendNickname)) {
            log.error("好友信息不存在,chatUserId:{},friendId:{},remark:{}", chatUserId, friendId, remark);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "好友信息不存在");
        }
        LambdaUpdateWrapper<ChatFriend> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(ChatFriend::getUserId, chatUserId)
                .eq(ChatFriend::getFriendId, friendId)
                .set(ChatFriend::getRemark, remark)
                .set(ChatFriend::getInitial, getInitial(remark, friendNickname));
        boolean updated = this.update(updateWrapper);
        if (!updated) {
            log.error("修改好友备注失败,chatUserId:{},friendId:{}", chatUserId, friendId);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "修改好友备注失败");
        }
        LambdaUpdateWrapper<ChatFriendApplyUserRel> updateWrapper1 = Wrappers.lambdaUpdate();
        updateWrapper1.eq(ChatFriendApplyUserRel::getUserId, chatUserId)
                .eq(ChatFriendApplyUserRel::getFriendId, friendId)
                .set(ChatFriendApplyUserRel::getRemark, remark);
        boolean updated1 = chatFriendApplyUserRelService.update(updateWrapper1);
        if (!updated1) {
            log.error("修改好友申请备注失败,chatUserId:{},friendId:{}", chatUserId, friendId);
        }
        return StringUtils.isBlank(remark) ? friendNickname : remark;
    }
}
