package com.dot.msg.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.msg.chat.model.ChatMsg;
import com.dot.msg.chat.model.ChatRoom;
import com.dot.msg.chat.model.ChatRoomUserRel;
import com.dot.msg.chat.request.ChatRoomAddRequest;
import com.dot.msg.chat.response.ChatRoomMsgInfoResponse;
import com.dot.msg.chat.response.ChatRoomUserResponse;
import com.dot.msg.chat.response.ChatUnreadCountResponse;

import java.util.List;

/**
 * 用户聊天室表服务接口
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
public interface ChatRoomService extends IService<ChatRoom> {

    String addChatRoom(ChatRoomAddRequest request);

    /**
     * 清理离线消息数
     *
     * @param chatIds 聊天室ID列表
     * @param userId  用户ID
     * @return true:清理成功;false:清理失败
     */
    boolean clearOfflineMsgCount(List<String> chatIds, Integer userId);

    /**
     * 批量更新离线消息数
     *
     * @param chatId  聊天室ID
     * @param userIds 用户ID列表
     * @param count   离线消息数
     * @return true:更新成功;false:更新失败
     */
    boolean batchUpdateOfflineMsgCount(String chatId, List<Integer> userIds, Integer count);

    /**
     * 清理未读消息数
     *
     * @param chatId 聊天室ID
     * @return true:清理成功;false:清理失败
     */
    boolean clearUnreadMsgCount(String chatId);

    /**
     * 清理未读消息数
     *
     * @param chatId 聊天室ID
     * @param userId 用户ID
     * @return true:清理成功;false:清理失败
     */
    boolean clearUnreadMsgCount(String chatId, Integer userId);

    /**
     * 更新未读消息数
     *
     * @param chatId        聊天室ID
     * @param unreadUserIds 未读用户ID列表
     * @param count         未读消息数
     * @return true:更新成功;false:更新失败
     */
    boolean updateUnreadMsgCount(String chatId, List<Integer> unreadUserIds, Integer count);

    boolean updateLastMsg(ChatMsg chatMsg);

    boolean clearLastMsgByMsgId(Integer msgId);

    boolean clearUserLastMsg(Integer userId, Integer msgId);

    /**
     * 是否存在用户聊天室
     *
     * @param chatId 聊天室ID
     * @param userId 用户ID
     * @return true:存在;false:不存在
     */
    boolean existUserChatRoom(String chatId, Integer userId);

    /**
     * 添加用户聊天室关系
     *
     * @param chatId 聊天室ID
     * @param userId 用户ID
     * @return true:添加成功;false:添加失败
     */
    boolean addChatRoomUserRel(String chatId, Integer userId);

    /**
     * 批量添加用户聊天室关系
     *
     * @param chatId  聊天室ID
     * @param userIds 用户ID列表
     * @return true:添加成功;false:添加失败
     */
    boolean addChatRoomUserRel(String chatId, List<Integer> userIds);

    List<ChatRoom> getList(List<String> chatIds);

    ChatRoom getByChatId(String chatId);

    /**
     * 获取聊天室用户关系列表
     *
     * @param chatId 聊天室ID
     * @return 聊天室用户关系列表
     */
    List<ChatRoomUserRel> getChatRoomUserRelListByChatId(String chatId);

    List<ChatRoomUserResponse> getChatRoomUserList();

    List<ChatRoomUserResponse> getChatRoomUserRefreshList();


    ChatUnreadCountResponse getChatUnreadCount();

    /**
     * 去发送信息,返回聊天室ID
     *
     * @param friendId 好友ID
     * @return 聊天室ID
     */
    String gotoSendMsg(Integer friendId);


    /**
     * 获取聊天室消息和好友申请未读数
     *
     * @return 聊天室消息和好友申请未读数
     */
    ChatRoomMsgInfoResponse getChatRoomMsgInfo(String chatId);

    /**
     * 更新消息免打扰
     *
     * @param chatId       聊天室ID
     * @param msgNoDisturb 消息免打扰
     * @return true:更新成功;false:更新失败
     */
    boolean updateMsgNoDisturb(String chatId, Boolean msgNoDisturb);

    /**
     * 更新是否置顶
     *
     * @param chatId   聊天室ID
     * @param isTop    是否置顶
     * @return true:更新成功;false:更新失败
     */
    boolean updateIsTop(String chatId, Boolean isTop);

    /**
     * 删除聊天室用户关系
     *
     * @param chatId 聊天室ID
     * @param userId 用户ID
     * @return true:删除成功;false:删除失败
     */
    boolean deleteChatRoomUserRel(String chatId, Integer userId);

    /**
     * 清空消息列表
     *
     * @param chatId 聊天id
     * @return 是否成功
     */
    boolean cleanMsgList(String chatId);

    /**
     * 删除聊天室
     *
     * @param chatId 聊天室ID
     * @return boolean
     */
    Boolean deleteChatRoom(String chatId);

}
