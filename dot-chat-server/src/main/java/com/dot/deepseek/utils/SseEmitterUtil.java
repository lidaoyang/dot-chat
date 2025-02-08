package com.dot.deepseek.utils;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sse工具
 *
 * @author: Dao-yang.
 * @date: Created in 2025/2/8 17:39
 */
public class SseEmitterUtil {

    private static final Map<Integer, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    public static boolean isExist(Integer userId) {
        return sseEmitterMap.containsKey(userId);
    }

    public static SseEmitter get(Integer userId) {
        return sseEmitterMap.get(userId);
    }

    public static SseEmitter add(Integer userId, SseEmitter sseEmitter) {
        sseEmitterMap.put(userId, sseEmitter);
        return sseEmitter;
    }

    public static void remove(Integer userId) {
        sseEmitterMap.remove(userId);
    }
}
