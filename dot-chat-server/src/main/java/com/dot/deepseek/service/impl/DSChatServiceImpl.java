package com.dot.deepseek.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.fastjson.JSON;
import com.dot.comm.manager.TokenManager;
import com.dot.deepseek.model.DeepseekReqRecord;
import com.dot.deepseek.request.DSChatRequest;
import com.dot.deepseek.service.DSChatService;
import com.dot.deepseek.service.DeepseekReqRecordService;
import com.dot.deepseek.utils.DSUtils;
import com.dot.deepseek.utils.SseEmitterUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

/**
 * @author: Dao-yang.
 * @date: Created in 2025/2/8 16:55
 */
@Slf4j
@Service
public class DSChatServiceImpl implements DSChatService {

    @Resource
    private DeepseekReqRecordService deepseekReqRecordService;

    @Resource
    private TokenManager tokenManager;

    @Override
    public String generateChatMessage(DSChatRequest request) {
        return DSUtils.generateChatMessage(request.getMessages());
    }

    @Override
    public SseEmitter generateChatMessageForStream(DSChatRequest request) {
        final Integer userId = tokenManager.getUserId();
        // 创建SseEmitter对象
        SseEmitter sseEmitter = SseEmitterUtil.add(userId, new SseEmitter(60000L * 2));
        // 保存请求记录
        final Integer reqRecordId = saveNewDeepseekReqRecord(request, userId);
        // 调用接口生成回复消息并发送到客户端
        DSUtils.generateChatMessageForStream(request.getMessages(), userId);
        sseEmitter.onCompletion(() -> {
            log.info("SseEmitter completed. uid:{},reqRecordId{}", userId, reqRecordId);
            // 更新请求记录
            updateDeepseekReqRecord(reqRecordId, userId);
            SseEmitterUtil.remove(userId);
        });
        sseEmitter.onTimeout(() -> {
            log.info("SseEmitter timeout. uid:{},reqRecordId:{}", userId, reqRecordId);
            SseEmitterUtil.remove(userId);
        });
        sseEmitter.onError(t -> {
            log.error("SseEmitter error. uid:{},reqRecordId:{}", userId, reqRecordId, t);
            SseEmitterUtil.remove(userId);
        });
        return sseEmitter;
    }

    private void updateDeepseekReqRecord(Integer reqRecordId, Integer userId) {
        DeepseekReqRecord updateRecord = new DeepseekReqRecord();
        updateRecord.setId(reqRecordId);
        updateRecord.setResMsg(SseEmitterUtil.getContent(userId));
        updateRecord.setResTime(LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN));
        deepseekReqRecordService.updateById(updateRecord);
    }

    private Integer saveNewDeepseekReqRecord(DSChatRequest request, Integer userId) {
        DeepseekReqRecord reqRecord = new DeepseekReqRecord();
        reqRecord.setUserId(userId);
        reqRecord.setChatId(request.getChatId());
        reqRecord.setReqMsg(JSON.toJSONString(request.getMessages()));
        reqRecord.setReqTime(LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN));
        deepseekReqRecordService.save(reqRecord);
        return reqRecord.getId();
    }

    @Override
    public boolean closeSse() {
        Integer userId = tokenManager.getUserId();
        if (SseEmitterUtil.isExist(userId)) {
            SseEmitterUtil.get(userId).complete();
        } else {
            log.info("用户[{}]连接已关闭", userId);
        }
        return true;
    }
}
