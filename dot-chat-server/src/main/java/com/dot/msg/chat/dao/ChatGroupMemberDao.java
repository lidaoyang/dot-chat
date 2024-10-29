package com.dot.msg.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dot.msg.chat.dto.ChatGroupInfoDto;
import com.dot.msg.chat.dto.ChatGroupMemberSimDto;
import com.dot.msg.chat.model.ChatGroupMember;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天室群成员表数据Mapper
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
public interface ChatGroupMemberDao extends BaseMapper<ChatGroupMember> {

    /**
     * 查询聊天室群组成员列表
     *
     * @param groupId 群组ID
     * @return 聊天室群组成员列表
     */
    @Select("select cgm.id,cgm.user_id,cgm.group_id,cgm.is_group_leader,cgm.is_group_manager,cgm.source,cgm.invite_user_id,cu.avatar,cu.is_online, " +
            "if(length(cf.remark)>0,cf.remark,if(length(cgm.nickname)>1,cgm.nickname,cu.nickname)) as nickname,cgm.nickname as groupNickname,if(cf.id is null ,false,true) isFriend " +
            "from chat_group_member cgm " +
            "inner join chat_user cu on cgm.user_id=cu.id " +
            "left join chat_friend cf on cf.friend_id=cu.id and cf.user_id=#{userId} " +
            "where cgm.group_id=#{groupId} ${andSql} " +
            "order by cgm.is_group_leader desc,cgm.is_group_manager desc, cgm.id")
    List<ChatGroupMemberSimDto> selectChatGroupMemberSimList(@Param("groupId") Integer groupId, @Param("userId") Integer userId, @Param("andSql") String andSql);

    /**
     * 查询聊天室群组成员头像列表
     *
     * @param groupId 群组ID
     * @return 聊天室群组成员列表
     */
    @Select("select cu.avatar " +
            "from chat_group_member cgm " +
            "inner join chat_user cu on cgm.user_id=cu.id " +
            "where cgm.group_id=#{groupId} " +
            "order by cgm.is_group_leader desc,cgm.is_group_manager desc, cgm.id")
    List<String> selectChatGroupMemberAvaterList(@Param("groupId") Integer groupId);


    /**
     * 查询聊天室群组信息
     *
     * @param groupId 群组ID
     * @return 聊天室群组成员列表
     */
    @Select("select cg.id,cg.name,cg.avatar,cg.invite_cfm,cg.group_leader_id,cg.managers,cg.member_count,cg.notice,cg.is_dissolve, " +
            "       cgm.nickname,cgm.is_group_leader,cgm.is_group_manager,cgm.invite_user_id,cgm.source " +
            "from chat_group cg " +
            "inner join chat_group_member cgm on cg.id = cgm.group_id " +
            "where cgm.group_id=#{groupId} and cgm.user_id=#{userId}")
    ChatGroupInfoDto selectChatGroupInfo(@Param("groupId") Integer groupId, @Param("userId") Integer userId);
}
