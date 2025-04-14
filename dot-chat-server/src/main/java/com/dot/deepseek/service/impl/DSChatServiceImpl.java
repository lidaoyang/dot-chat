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
import com.dot.sse.MySseEmitter;
import com.dot.sse.OkHttpSSEClient;
import com.dot.sse.em.EventSourceKeyEm;
import com.dot.sse.listener.DeepSeekEventSourceListener;
import com.dot.sse.util.SseEmitterUtil;
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
        // 保存请求记录
        final Integer reqRecordId = saveNewDeepseekReqRecord(request, userId);

        // EventSourceListener
        DeepSeekEventSourceListener eventSourceListener = new DeepSeekEventSourceListener(userId);

        // 调用接口生成回复消息并发送到客户端
        OkHttpSSEClient.getInstance().startByPost(EventSourceKeyEm.DEEPSEEK, DSUtils.getDSChatUrl(), DSUtils.getHeader(), DSUtils.getJsonBody(request.getMessages()), eventSourceListener);

        // 创建sseEmitter
        return SseEmitterUtil.connect(userId, 60000L * 2, new MySseEmitter.SseStateListener() {
            @Override
            public void onCompletion() {
                log.info("SSE请求完成, 更新记录. uid:{},reqRecordId:{}", userId, reqRecordId);
                updateDeepseekReqRecord(reqRecordId, userId, eventSourceListener.getFullContent());
            }
        });
    }

    private void updateDeepseekReqRecord(Integer reqRecordId, Integer userId, String resMsg) {
        DeepseekReqRecord updateRecord = new DeepseekReqRecord();
        updateRecord.setId(reqRecordId);
        updateRecord.setResMsg(resMsg);
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
        SseEmitterUtil.closeSSE(userId);
        return true;
    }
}
