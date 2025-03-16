package com.dot.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dot.chat.dto.ChatUserRegisterCountDto;
import com.dot.chat.model.ChatUser;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天室用户表(关联管理员表和企业用户表)数据Mapper
 * 
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
public interface ChatUserDao extends BaseMapper<ChatUser> {

    @Select("SELECT create_date as date,COUNT(id) AS num FROM chat_user GROUP BY date ORDER BY date desc")
    List<ChatUserRegisterCountDto> selectUserRegisterCount();
}
