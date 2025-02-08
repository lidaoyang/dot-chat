package com.dot.deepseek.service.impl;

import com.dot.comm.manager.TokenManager;
import com.dot.deepseek.request.DSChatRequest;
import com.dot.deepseek.service.DSChatService;
import com.dot.deepseek.utils.DSUtils;
import com.dot.deepseek.utils.SseEmitterUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author: Dao-yang.
 * @date: Created in 2025/2/8 16:55
 */
@Slf4j
@Service
public class DSChatServiceImpl implements DSChatService {

    @Resource
    private TokenManager tokenManager;

    @Override
    public String generateChatMessage(DSChatRequest request) {
        return DSUtils.generateChatMessage(request.getMessages());
    }

    @Override
    public SseEmitter generateChatMessageForStream(DSChatRequest request) {
        Integer userId = tokenManager.getUserId();
        SseEmitter sseEmitter = SseEmitterUtil.add(userId, new SseEmitter(60000L * 2));
        DSUtils.generateChatMessageForStream(request.getMessages(), userId);
        sseEmitter.onCompletion(() -> {
            log.info("SseEmitter completed. uid:{}", userId);
            SseEmitterUtil.remove(userId);
        });
        sseEmitter.onTimeout(() -> {
            log.info("SseEmitter timeout. uid:{}", userId);
            SseEmitterUtil.remove(userId);
        });
        sseEmitter.onError(t -> {
            log.error("SseEmitter error. uid:{}", userId, t);
            SseEmitterUtil.remove(userId);
        });
        return sseEmitter;
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
