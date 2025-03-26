package com.dot.chat.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dot.chat.dto.ChatRoomDto;
import com.dot.chat.model.ChatRoom;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户聊天室表数据Mapper
 * 
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
public interface ChatRoomDao extends BaseMapper<ChatRoom> {

    @Select("""
            select cr.id,cr.chat_id,cr.group_id,crur.last_time from chat_room cr
            left join(select distinct chat_id,user_id,last_time from chat_room_user_rel) crur on crur.chat_id=cr.chat_id
            ${ew.customSqlSegment}
            """)
    IPage<ChatRoomDto> selectChatRoomList(Page<ChatRoom> page, @Param(Constants.WRAPPER) QueryWrapper<ChatRoom> queryWrapper);
}
