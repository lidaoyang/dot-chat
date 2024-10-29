package com.dot.msg.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dot.msg.chat.dto.ChatUserSimDto;
import com.dot.msg.chat.model.ChatUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天室用户表(关联管理员表和企业用户表)数据Mapper
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
public interface ChatUserDao extends BaseMapper<ChatUser> {

    /**
     * 查询聊天室用户列表(左链接好友)
     *
     * @return 聊天室用户列表
     */
    @Select("select cu.id, if(length(cf.remark)>0,cf.remark,cu.nickname)as nickname,cu.nickname as olnickname,cu.avatar,cu.is_online,if(cf.id is null,true,false) isFriend " +
            "from chat_user cu " +
            "left join chat_friend cf on cf.friend_id=cu.id and cf.user_id=#{currentUserId} " +
            "where cu.id in(${chatUserIds})")
    List<ChatUserSimDto> selectChatUserLeftFriendSimList(@Param("chatUserIds") String chatUserIds, @Param("currentUserId") Integer currentUserId);

}
