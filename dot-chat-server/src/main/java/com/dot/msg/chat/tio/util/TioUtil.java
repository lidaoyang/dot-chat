package com.dot.msg.chat.tio.util;

import cn.hutool.core.collection.CollUtil;
import com.dot.comm.constants.CommConstant;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.TioConfig;
import org.tio.utils.lock.SetWithLock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Tio工具类
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/25 10:23
 */
@Slf4j
public class TioUtil {

    /**
     * 判断用户是否在线
     *
     * @param tioConfig 服务器Tio配置
     * @param userId    用户ID
     * @return 是否在线
     */
    public static boolean isOnline(TioConfig tioConfig, String userId) {
        boolean isOnline = true;
        SetWithLock<ChannelContext> contextSetWithLock = Tio.getByUserid(tioConfig, userId);
        if (contextSetWithLock == null) {
            isOnline = false;
            log.error("接收用户处于离线状态,toUserId:{}", userId);
        }
        return isOnline;
    }

    /**
     * 判断用户是否离线
     *
     * @param tioConfig 服务器Tio配置
     * @param userId    用户ID
     * @return 是否离线
     */
    public static boolean isOffline(TioConfig tioConfig, String userId) {
        return !isOnline(tioConfig, userId);
    }

    /**
     * 设置当前用户在聊天室列表选中的聊天室ID
     *
     * @param tioConfig  服务器Tio配置
     * @param userId     用户ID
     * @param currChatId 当前聊天室ID
     */
    public static void setCurrChatId(TioConfig tioConfig, String userId, String currChatId) {
        setAttribute(tioConfig, userId, CommConstant.CURR_CHAT_ID_KEY, currChatId);
    }

    /**
     * 获取当前用户在聊天室列表选中的聊天室ID
     *
     * @param tioConfig 服务器Tio配置
     * @param userId    用户ID
     * @return 聊天室ID
     */
    public static String getCurrChatId(TioConfig tioConfig, String userId) {
        Object obj = getAttribute(tioConfig, userId, CommConstant.CURR_CHAT_ID_KEY);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    /**
     * 移除当前用户在聊天室列表选中的聊天室ID
     *
     * @param tioConfig 服务器Tio配置
     * @param userId    用户ID
     */
    public static void removeCurrChatId(TioConfig tioConfig, String userId) {
        removeAttribute(tioConfig, userId, CommConstant.CURR_CHAT_ID_KEY);
    }

    /**
     * 设置当前用户在好友申请列表选中的好友申请ID
     *
     * @param tioConfig         服务器Tio配置
     * @param userId            用户ID
     * @param currFriendApplyId 当前好友申请ID
     */
    public static void setCurrFriendApplyId(TioConfig tioConfig, String userId, Integer currFriendApplyId) {
        setAttribute(tioConfig, userId, CommConstant.CURR_FRIEND_APPLY_ID_KEY, currFriendApplyId);
    }

    /**
     * 移除当前用户在好友申请列表选中的好友申请ID
     *
     * @param tioConfig 服务器Tio配置
     * @param userId    用户ID
     */
    public static void removeCurrFriendApplyId(TioConfig tioConfig, String userId) {
        removeAttribute(tioConfig, userId, CommConstant.CURR_FRIEND_APPLY_ID_KEY);
    }

    /**
     * 获取当前用户在好友申请列表选中的好友申请ID
     *
     * @param tioConfig 服务器Tio配置
     * @param userId    用户ID
     * @return 申请ID
     */
    public static Integer getCurrFriendApplyId(TioConfig tioConfig, String userId) {
        Object obj = getAttribute(tioConfig, userId, CommConstant.CURR_FRIEND_APPLY_ID_KEY);
        if (obj == null) {
            return null;
        }
        return (Integer) obj;
    }

    /**
     * 设置属性值
     *
     * @param tioConfig 服务器Tio配置
     * @param userId    用户ID
     * @param key       属性名
     * @param value     值
     */
    public static void setAttribute(TioConfig tioConfig, String userId, String key, Object value) {
        SetWithLock<ChannelContext> channelContexts = Tio.getByUserid(tioConfig, userId);
        if (channelContexts == null) {
            log.error("用户处于离线状态,userId:{}", userId);
            return;
        }
        channelContexts.getObj().stream().findFirst().ifPresent(channelContext -> {
            channelContext.setAttribute(key, value);
        });
    }

    /**
     * 获取属性值
     *
     * @param tioConfig 服务器Tio配置
     * @param userId    用户ID
     * @param key       属性名
     * @return value
     */
    public static Object getAttribute(TioConfig tioConfig, String userId, String key) {
        SetWithLock<ChannelContext> channelContexts = Tio.getByUserid(tioConfig, userId);
        if (channelContexts == null) {
            log.error("用户处于离线状态,userId:{}", userId);
            return null;
        }
        Optional<ChannelContext> first = channelContexts.getObj().stream().findFirst();
        if (first.isPresent()) {
            ChannelContext channelContext = first.get();
            return channelContext.getAttribute(key);
        }
        return null;
    }

    /**
     * 移除属性值
     *
     * @param tioConfig 服务器Tio配置
     * @param userId    用户ID
     * @param key       属性名
     */
    public static void removeAttribute(TioConfig tioConfig, String userId, String key) {
        SetWithLock<ChannelContext> channelContexts = Tio.getByUserid(tioConfig, userId);
        if (channelContexts == null) {
            log.error("用户处于离线状态,userId:{}", userId);
            return;
        }
        channelContexts.getObj().stream().findFirst().ifPresent(channelContext -> {
            channelContext.removeAttribute(key);
        });
    }

    public static ChannelContext getChannelContext(TioConfig tioConfig, String userId) {
        SetWithLock<ChannelContext> channelContexts = Tio.getByUserid(tioConfig, userId);
        if (channelContexts != null) {
            Optional<ChannelContext> first = channelContexts.getObj().stream().findFirst();
            if (first.isPresent()) {
                return first.get();
            }
        }
        return null;
    }

    /**
     * 获取聊天室ID
     *
     * @param sendUserId 发送用户ID
     * @param toUserId   接收用户ID
     * @return 聊天室ID
     */
    public static String generateChatId(Integer sendUserId, Integer toUserId) {
        List<Integer> userIds = new ArrayList<>();
        userIds.add(sendUserId);
        userIds.add(toUserId);
        Collections.sort(userIds);
        return CollUtil.join(userIds, "_");
    }

    public static List<Integer> getChatUserIdsByChatId(String chatId) {
        if (chatId.contains("G")) {
            log.error("群聊ID:{}", chatId);
            throw new ApiException(ExceptionCodeEm.PRAM_NOT_MATCH, "只支持单聊ID");
        }
        String[] userIds = chatId.split("_");
        return CollUtil.newArrayList(Integer.valueOf(userIds[0]), Integer.valueOf(userIds[1]));
    }

    /**
     * 获取聊天室ID
     *
     * @param groupId 群ID
     * @return 聊天室ID
     */
    public static String generateChatId(Integer groupId) {
        return "G_" + groupId;
    }

    public static Integer getGroupIdByChatId(String chatId) {
        return Integer.valueOf(chatId.substring(2));
    }

    /**
     * 判断是否未读
     *
     * @param tioConfig 服务器Tio配置
     * @param userId    用户ID
     * @param chatId    聊天室ID
     * @return true:未读
     */
    public static boolean isUnread(TioConfig tioConfig, Integer userId, String chatId) {
        String currChatId = TioUtil.getCurrChatId(tioConfig, userId.toString());
        log.info("currChatId:{},chatId:{},userId:{}", currChatId, chatId, userId);
        return !chatId.equals(currChatId);
    }
}
