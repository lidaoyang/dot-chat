package com.dot.msg.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dot.msg.chat.dto.ChatUserFriendDto;
import com.dot.msg.chat.model.ChatFriend;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天室好友表数据Mapper
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
public interface ChatFriendDao extends BaseMapper<ChatFriend> {


    @Select("select cu.nickname,cu.signature,cu.avatar, cu.sex, cf.remark, cf.friend_id, cf.is_top, cf.initial " +
            "from chat_friend cf " +
            "inner join chat_user cu on cu.id=cf.friend_id " +
            "where cf.user_id=#{userId} ${andSql} " +
            "order by cf.is_top desc, cf.initial, cu.nickname ")
    List<ChatUserFriendDto> selectChatUserFriendList(@Param("userId") Integer currentUserId, @Param("andSql") String andSql);

    @Select("select cu.nickname, cu.avatar, cu.sex,cu.signature, cf.friend_id,cf.remark,cf.label,cf.source " +
            "from chat_friend cf " +
            "inner join chat_user cu on cu.id=cf.friend_id " +
            "where cf.user_id=#{userId} and cf.friend_id=#{friendId}")
    ChatUserFriendDto selectChatUserFriendInfo(@Param("userId") Integer currentUserId, @Param("friendId") Integer friendId);


}
