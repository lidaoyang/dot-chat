package com.dot.msg.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.msg.chat.model.ChatFriend;
import com.dot.msg.chat.request.ChatFriendAddRequest;
import com.dot.msg.chat.response.ChatUserFriendInfoResponse;
import com.dot.msg.chat.response.ChatUserFriendResponse;

import java.util.List;

/**
 * 聊天室好友表服务接口
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
public interface ChatFriendService extends IService<ChatFriend> {

    /**
     * 获取当前登录用户的好友列表
     *
     * @return 好友列表
     */
    List<ChatUserFriendResponse> getChatUserFriendList(String keywords);

    /**
     * 获取当前登录用户的好友列表,查询好友ID列表中的好友或过滤掉群成员
     *
     * @param userId    用户ID
     * @param friendIds 好友id列表
     * @return 好友列表
     */
    List<ChatUserFriendResponse> getChatUserFriendList(Integer userId, List<Integer> friendIds);

    /**
     * 获取当前登录用户的好友列表,并过滤掉群成员
     *
     * @param filterGroupId 过滤群组ID
     * @param keywords      搜索关键词(id,nickname)
     * @return 好友列表
     */
    List<ChatUserFriendResponse> getChatUserFriendList(Integer filterGroupId, String keywords);

    /**
     * 获取当前登录用户的好友列表,并过滤掉群成员
     *
     * @param userId        用户ID
     * @param filterGroupId 过滤群组ID
     * @param keywords      搜索关键词(id,nickname)
     * @return 好友列表
     */
    List<ChatUserFriendResponse> getChatUserFriendList(Integer userId, Integer filterGroupId, String keywords);

    /**
     * 获取当前登录用户的好友列表,查询好友ID列表中的好友或过滤掉群成员
     *
     * @param userId        用户ID
     * @param friendIds     好友id列表
     * @param filterGroupId 过滤群组ID
     * @param keywords      搜索关键词(id,nickname)
     * @return 好友列表
     */
    List<ChatUserFriendResponse> getChatUserFriendList(Integer userId, List<Integer> friendIds, Integer filterGroupId, String keywords);

    /**
     * 获取当前登录用户的好友详情
     *
     * @param friendId 好友id
     * @return 好友详情
     */
    ChatUserFriendInfoResponse getChatUserFriendInfo(Integer friendId);

    boolean addChatFriend(ChatFriendAddRequest request);

    /**
     * 获取好友id列表
     *
     * @param userId userId
     * @return 好友id列表
     */
    List<Integer> getFriendIds(Integer userId);

    /**
     * 修改好友备注
     *
     * @param friendId 好友id
     * @param remark   好友备注
     * @return nickname
     */
    String modifyFriendRemark(Integer friendId, String remark);

}
