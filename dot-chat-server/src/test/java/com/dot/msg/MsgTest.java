package com.dot.msg;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.dot.comm.utils.CommUtil;
import com.dot.msg.chat.em.ChatTypeEm;
import com.dot.msg.chat.request.ChatMsgAddRequest;
import com.dot.msg.chat.tio.em.MsgTypeEm;
import com.dot.msg.chat.tio.entiy.TioMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/23 11:53
 */
public class MsgTest {


    @Test
    public void test1(){
        TioMessage message = new TioMessage();
        message.setChatId("1111");
        message.setMsgType(MsgTypeEm.SYSTEM);
        message.setMsg("aaadddd对对对");
        message.setChatType(ChatTypeEm.GROUP);
        message.setSendUserId(111);
        message.setToUserId(222);

        ChatMsgAddRequest msgAddRequest = new ChatMsgAddRequest();
        msgAddRequest.setChatId(message.getChatId());
        msgAddRequest.setChatType(message.getChatType().name());
        msgAddRequest.setMsgType(message.getMsgType().name());
        msgAddRequest.setMsg(message.getMsg());
        msgAddRequest.setSendUserId(message.getSendUserId());
        msgAddRequest.setToUserId(message.getToUserId());

        message.setToUserId(3333);

        System.out.println(JSON.toJSONString(msgAddRequest));
        System.out.println(JSON.toJSONString(message));
    }

    @Test
    public void test2(){
        List<Integer> set = new ArrayList<>();
        set.add(1001);
        set.add(998);
        set.add(220);
        set.add(1003);
        Collections.sort(set);
        System.out.println(set);
    }

    @Test
    public void test3(){
        List<String> set = new ArrayList<>();
        set.add("1001,2222");
        set.add("998");
        set.add("220,333");
        set.add("1003,111");

        List<String> collect = set.stream().flatMap(str -> CommUtil.stringToArrayStr(str).stream()).collect(Collectors.toList());
        System.out.println(collect);
    }

    @Test
    public void test4(){
        DateTime dateTime = DateUtil.parse("2024-06-19 23:59:59");
        long days = DateUtil.betweenDay(DateUtil.date(), dateTime, true);
        System.out.println(days);

        List<String> monthList = new ArrayList<>();
        for (DateTime start = dateTime; start.isBefore(DateUtil.date()); start = start.offset(DateField.MONTH, 1)) {
            monthList.add(start.toString(DatePattern.SIMPLE_MONTH_PATTERN));
        }
        if (CollUtil.isEmpty(monthList)) {
            dateTime =dateTime.offset(DateField.MONTH, 1);
            monthList.add(dateTime.toString(DatePattern.SIMPLE_MONTH_PATTERN));
        }
        System.out.println(monthList);
    }
}
