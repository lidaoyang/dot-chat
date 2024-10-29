package com.dot.msg.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.comm.em.UserTypeEm;
import com.dot.msg.chat.model.ChatFriendApply;
import com.dot.msg.chat.request.ChatFriendApplyAddRequest;
import com.dot.msg.chat.request.ChatFriendApplyAgreeRequest;
import com.dot.msg.chat.request.ChatFriendApplyReplayRequest;
import com.dot.msg.chat.response.ChatFriendApplyInfoResponse;
import com.dot.msg.chat.response.ChatFriendApplyResponse;

import java.util.List;

/**
 * 聊天室新好友申请表服务接口
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
public interface ChatFriendApplyService extends IService<ChatFriendApply> {

    /**
     * 获取当前登录用户的好友申请列表
     *
     * @param userType 用户类型
     * @return 好友申请列表
     */
    List<ChatFriendApplyResponse> getChatFriendApplyList(UserTypeEm userType);

    /**
     * 获取好友申请详情
     *
     * @param userType 用户类型
     * @param applyId  申请ID
     * @return 申请详情
     */
    ChatFriendApplyInfoResponse getChatFriendApplyInfo(UserTypeEm userType, Integer applyId);

    /**
     * 添加好友申请
     *
     * @param request 添加好友申请请求
     * @return true 成功，false 失败
     */
    boolean addChatFriendApply(ChatFriendApplyAddRequest request);

    /**
     * 回复好友申请
     *
     * @param request 请求参数
     * @return true 成功，false 失败
     */
    boolean replayFriendApply(ChatFriendApplyReplayRequest request);

    /**
     * 同意好友申请
     *
     * @return chatId
     */
    String agreeFriendApply(ChatFriendApplyAgreeRequest request);

    /**
     * 删除好友申请
     *
     * @param applyId 申请ID
     * @return true 成功，false 失败
     */
    boolean deleteFriendApply(UserTypeEm userType, Integer applyId);

    /**
     * 清空未读数
     *
     * @param applyId 申请ID
     * @return true 成功，false 失败
     */
    boolean clearUnreadCount(UserTypeEm userType, Integer applyId);

    /**
     * 清空未读数
     *
     * @param applyId 申请ID
     * @param userId 用户ID
     * @return true 成功，false 失败
     */
    boolean clearUnreadCount(Integer applyId, Integer userId);
}
