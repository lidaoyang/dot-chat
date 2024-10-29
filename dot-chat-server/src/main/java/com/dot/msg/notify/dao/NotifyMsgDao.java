package com.dot.msg.notify.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dot.msg.notify.dto.NotifyMsgDto;
import com.dot.msg.notify.dto.NotifyMsgInfoDto;
import com.dot.msg.notify.model.NotifyMsg;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 通知消息数据Mapper
 *
 * @author Dao-yang
 * @date: 2024-01-30 14:44:18
 */
public interface NotifyMsgDao extends BaseMapper<NotifyMsg> {

    @Select("select cnmur.id,cnm.id as msgid, cnm.linkid, cnm.msg_content, cnm.send_time,cnmur.send_user_id,cnmur.to_user_id,cnmur.is_read " +
            "from chat_notify_msg cnm " +
            "inner join chat_notify_msg_user_rel cnmur on cnm.id=cnmur.notify_msg_id " +
            "where cnm.linkid=#{linkid} and cnm.msg_type=#{msgType} and cnm.event_type=#{eventType} and cnmur.user_id=#{userId} " +
            "order by cnm.id desc " +
            "limit #{limit}")
    List<NotifyMsgDto> selectNotifyMsgList(@Param("linkid") String linkid, @Param("userId") Integer userId, @Param("msgType") String msgType, @Param("eventType") String eventType, @Param("limit") Integer limit);
    @Select("select cnmur.id,cnm.id as msgid, cnm.linkid, cnm.msg_content, cnm.send_time,cnmur.send_user_id,cnmur.to_user_id,cnmur.is_read " +
            "from chat_notify_msg cnm " +
            "inner join chat_notify_msg_user_rel cnmur on cnm.id=cnmur.notify_msg_id " +
            "where cnmur.is_read=false and cnm.linkid=#{linkid} and cnm.msg_type=#{msgType} and cnm.event_type=#{eventType} and cnmur.user_id=#{userId} " +
            "order by cnm.id desc " +
            "limit #{limit}")
    List<NotifyMsgDto> selectUnReadNotifyMsgList(@Param("linkid") String linkid, @Param("userId") Integer userId, @Param("msgType") String msgType, @Param("eventType") String eventType, @Param("limit") Integer limit);

    @Select("select cnm.id, cnm.linkid,cnm.event_type,cnm.msg_type,cnm.notify_type, cnm.msg_content, cnm.send_time, " +
            "cnmur.send_user_id,cnmur.to_user_id,cnmur.is_offline,cnmur.is_read " +
            "from chat_notify_msg cnm " +
            "inner join chat_notify_msg_user_rel cnmur on cnm.id=cnmur.notify_msg_id " +
            "where  cnmur.user_id=#{userId} and cnmur.is_offline=1 " +
            "order by cnm.id desc ")
    List<NotifyMsgInfoDto> selectOfflineNotifyMsgList(@Param("userId") Integer userId);


}
