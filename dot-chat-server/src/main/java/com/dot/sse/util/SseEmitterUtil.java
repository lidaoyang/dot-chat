package com.dot.sse.util;

import com.dot.sse.MySseEmitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sse工具
 *
 * @author: Dao-yang.
 * @date: Created in 2025/2/8 17:39
 */
@Slf4j
public class SseEmitterUtil {

    private static final Map<Integer, MySseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    public static boolean isExist(Integer userId) {
        return sseEmitterMap.containsKey(userId);
    }

    public static MySseEmitter get(Integer userId) {
        return sseEmitterMap.get(userId);
    }

    public static MySseEmitter add(Integer userId, MySseEmitter sseEmitter) {
        sseEmitterMap.put(userId, sseEmitter);
        return sseEmitter;
    }

    public static void remove(Integer userId) {
        MySseEmitter emitter = get(userId);
        if (emitter != null && emitter.isCancelled()) {
            sseEmitterMap.remove(userId);
        }
    }

    public static int getConnectedSize() {
        return sseEmitterMap.size();
    }

    public static SseEmitter connect(Integer userId, Long timeout, MySseEmitter.SseStateListener listener) {
        if (isExist(userId)) {
            log.info("sse 连接已存在,关闭后重新连接，userId:{}", userId);
            closeSSE(userId);
            return get(userId);
        }
        // 0 表示无限长连接；其他：毫秒数，表示连接时长，比如 1000L，就是 1秒后断开连接
        MySseEmitter emitter = new MySseEmitter(timeout, listener);

        // sse 连接完成，准备释放
        emitter.oncomplete(userId);

        // sse 连接超时
        emitter.ontimeout(userId);

        // 指定当发生错误时执行的回调方法。这个错误可能是由于网络连接问题等原因。
        emitter.onerror(userId);

        // 添加 map，用于发送给多个 sse 客户端
        add(userId, emitter);

        log.info("sse 连接成功！userId:{}", userId);
        return emitter;
    }

    /**
     * 发送消息
     */
    public static void send(Integer userId, Object message) {
        MySseEmitter sseEmitter = get(userId);
        if (sseEmitter == null) {
            log.info("sse 未连接或已关闭，userId:{}", userId);
            return;
        }
        send(sseEmitter, message);
    }

    /**
     * 发送消息
     */
    public static void send(MySseEmitter sseEmitter, Object message) {
        try {
            if (sseEmitter.isCancelled()) {
                log.info("sse 已关闭");
                sseEmitter.complete();
                return;
            }
            // 发送信息
            sseEmitter.send(message);
        } catch (IOException e) {
            // 关闭连接
            log.debug("sse 发送消息失败！", e);
            sseEmitter.cancel();
        }
    }

    /**
     * 批量发送消息
     */
    public static void batchSend(Object message) {
        log.info("sse 批量发送消息！");
        sseEmitterMap.values().forEach(sseEmitter -> send(sseEmitter, message));
    }

    public static void closeSSE(Integer userId) {
        if (isExist(userId)) {
            log.info("关闭连接，userId:{}", userId);
            // 关闭连接
            get(userId).cancel();
            remove(userId);
        }
    }
}
