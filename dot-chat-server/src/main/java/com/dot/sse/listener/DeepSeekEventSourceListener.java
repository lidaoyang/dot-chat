package com.dot.sse.listener;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import com.dot.sse.MySseEmitter;
import com.dot.sse.OkHttpSSEClient;
import com.dot.sse.em.EventSourceKeyEm;
import com.dot.sse.util.SseEmitterUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * DeepSeek智能回复事件监听
 *
 * @author: Dao-yang.
 * @date: Created in 2025/3/27 13:44
 */
@Slf4j
public class DeepSeekEventSourceListener extends BaseEventSourceListener {

    private final StringBuilder fullContent = new StringBuilder();

    private final Integer userId;


    public DeepSeekEventSourceListener(Integer userId) {
        this.userId = userId;
    }

    @Override
    public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
        log.debug("SSE connected to: {}", response.request().url());
        // 连接成功 设置取消标志为false
        super.connected();
    }

    @Override
    public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type,
                        @NotNull String data) {
        log.debug("Received data: {}", data);
        MySseEmitter sseEmitter = SseEmitterUtil.get(userId);
        if (ObjectUtil.isNull(sseEmitter) || sseEmitter.isCancelled()) {
            log.info("sse 未连接或已关闭");
            OkHttpSSEClient.getInstance().close(EventSourceKeyEm.DEEPSEEK);
            return;
        }
        if ("[DONE]".equals(data)) {
            log.info("数据接收完成,关闭请求");
            sseEmitter.cancel();
            return;
        }
        JSONObject dataJ = JSONObject.parseObject(data);
        String content = dataJ.getJSONArray("choices").getJSONObject(0).getJSONObject("delta").getString("content");
        if (StringUtils.isNotBlank(content)) {
            if (sseEmitter.isCancelled()) {
                log.info("sse 已关闭");
                sseEmitter.complete();
                return;
            }
            try {
                fullContent.append(content);
                sseEmitter.send(content);
            } catch (IOException e) {
                // 关闭连接
                log.debug("sse 发送失败,关闭连接", e);
                sseEmitter.cancel();
            }

        }
    }

    @Override
    public void onClosed(@NotNull EventSource eventSource) {
        log.info("SSE connection closed");
        OkHttpSSEClient.getInstance().cancel(EventSourceKeyEm.DEEPSEEK);
    }

    @Override
    public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
        if (isCancelled()) {
            log.info("SSE connection cancelled");
            return;
        }
        log.error("SSE connection failed", t);
        // 重新连接
        OkHttpSSEClient.getInstance().scheduleReconnect(EventSourceKeyEm.DEEPSEEK, eventSource.request().url().toString(), this);
    }

    public String getFullContent() {
        return fullContent.toString();
    }

}
