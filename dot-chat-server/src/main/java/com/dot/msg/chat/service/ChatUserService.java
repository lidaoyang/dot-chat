package com.dot.msg.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.comm.em.UserTypeEm;
import com.dot.comm.entity.LoginUsername;
import com.dot.msg.chat.dto.ChatUserSimDto;
import com.dot.msg.chat.model.ChatUser;
import com.dot.msg.chat.response.ChatUserInfoResponse;
import com.dot.msg.chat.response.ChatUserResponse;
import com.dot.msg.chat.response.ChatUserSearchResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 聊天室用户表(关联管理员表和企业用户表)服务接口
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
public interface ChatUserService extends IService<ChatUser> {

    List<ChatUser> getList(List<Integer> userIds);

    List<ChatUser> getSimList(List<Integer> userIds);

    List<ChatUserSimDto> getChatUserLeftFriendSimList(List<Integer> userIds, Integer currentUserId);

    List<Integer> getAllChatUserIds();

    /**
     * 根据用户id和类型获取聊天室用户
     *
     * @param userId   用户ID
     * @param userType 用户类型(ENTERPRISE:服务商企业;SUPPLIER:供应商;PL_ADMIN:平台管理员;ENT_USER:企业用户)
     * @return ChatUser
     **/
    ChatUser getChatUser(Integer userId, String userType);

    /**
     * 根据用户id和类型获取聊天室用户
     *
     * @param userId   用户ID
     * @param userType 用户类型(ENTERPRISE:服务商企业;SUPPLIER:供应商;PL_ADMIN:平台管理员;ENT_USER:企业用户)
     * @return ChatUser
     **/
    Integer getChatUserId(Integer userId, String userType);

    /**
     * 获取当前聊天室用户
     *
     * @param userType 用户类型
     * @return ChatUser
     */
    ChatUserResponse getCurrentChatUser(UserTypeEm userType);

    /**
     * 获取当前聊天室用户ID
     *
     * @param userType 用户类型
     * @return ChatUser
     */
    Integer getCurrentChatUserId(UserTypeEm userType);

    String getNicknameById(Integer id);

    /**
     * 添加聊天室用户
     *
     * @param loginUser 用户登录信息
     * @return ChatUser
     */
    ChatUser addChatUser(LoginUsername loginUser);

    /**
     * 更新聊天室用户在线状态
     *
     * @param chatUserId 聊天室用户ID
     * @param isOnline   是否在线
     * @return boolean
     */
    boolean updateOnlineStatus(Integer chatUserId, boolean isOnline);

    /**
     * 获取聊天室用户列表-不包含好友(用户搜索添加)
     *
     * @param userType 用户类型
     * @param keyword  关键词
     * @return 用户列表
     */
    List<ChatUserSearchResponse> getSearchChatUserList(UserTypeEm userType, String keyword);

    ChatUserInfoResponse getChatUserInfo(UserTypeEm userType, Integer userId);

    List<Integer> getChatUserIds(UserTypeEm userType, Integer enterpriseId);

    boolean updatePhoneAndEnterpriseId(Integer id, String userType, Integer enterpriseId, String account);

    /**
     * 更新聊天室用户头像
     *
     * @param imgFile  图片文件
     * @param userType 用户类型
     */
    String updateAvatar(MultipartFile imgFile, UserTypeEm userType);

    /**
     * 更新聊天室用户昵称
     *
     * @param userType 用户类型
     * @param nickname 昵称
     * @param sex      性别
     * @return boolean
     */
    Boolean updateNickname(UserTypeEm userType, String nickname, Integer sex);

}
