package com.dot.sse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.Method;
import com.dot.comm.utils.CommUtil;
import com.dot.sse.em.EventSourceKeyEm;
import com.dot.sse.listener.BaseEventSourceListener;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSources;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * SSE 工具类
 *
 * @author: Dao-yang.
 * @date: Created in 2025/3/27 11:31
 */
@Slf4j
public class OkHttpSSEClient {

    private static OkHttpSSEClient instance;

    private final OkHttpClient client;

    private final Map<EventSourceKeyEm, EventSourceAndListenerObj> eventSourceMap = new ConcurrentHashMap<>();

    private int retryCount = 0;
    private static final int MAX_RETRIES = 5;
    private static final long BASE_RETRY_DELAY_MS = 1000; // 初始重试延迟1秒

    public static OkHttpSSEClient getInstance() {
        if (instance == null) {
            instance = new OkHttpSSEClient();
        }
        return instance;
    }

    private OkHttpSSEClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS) // 长连接，不超时
                .build();
    }

    /**
     * 启动/重启 SSE 连接 (GET)
     *
     * @param eventSourceKey      事件源的键
     * @param url                 新的 SSE 地址
     * @param eventSourceListener 事件源监听器
     */
    public void start(EventSourceKeyEm eventSourceKey, String url,
                      BaseEventSourceListener eventSourceListener) {
        // 关闭旧连接（如果存在）
        closeExists(eventSourceKey);

        // 创建新的请求
        Request request = getRequestBuilder(Method.GET, url, null, null).build();

        // 创建新的 EventSource
        start(eventSourceKey, url, eventSourceListener, request);
    }

    /**
     * 启动/重启 SSE 连接(POST)
     *
     * @param eventSourceKey      事件源的键
     * @param url                 新的 SSE 地址
     * @param headers             请求头
     * @param jsonBody            请求体
     * @param eventSourceListener 事件源监听器
     */
    public void startByPost(EventSourceKeyEm eventSourceKey, String url, Map<String, String> headers, String jsonBody,
                            BaseEventSourceListener eventSourceListener) {
        // 关闭旧连接（如果存在）
        closeExists(eventSourceKey);

        // 创建新的请求
        Request request = getRequestBuilder(Method.POST, url, headers, jsonBody).build();

        start(eventSourceKey, url, eventSourceListener, request);
    }

    private void closeExists(EventSourceKeyEm eventSourceKey) {
        if (eventSourceMap.containsKey(eventSourceKey)) {
            log.info("正在关闭旧连接[{}]", eventSourceKey.getDesc());
            close(eventSourceKey);
        }
    }

    private void start(EventSourceKeyEm eventSourceKey, String url, BaseEventSourceListener eventSourceListener,
                       Request request) {
        // 创建新的 EventSource
        EventSource.Factory factory = EventSources.createFactory(client);
        EventSource eventSource = factory.newEventSource(request, eventSourceListener);
        // 将新的 EventSource 添加到 Map 中
        eventSourceMap.put(eventSourceKey, new EventSourceAndListenerObj(eventSource, eventSourceListener));
        log.info("SSE 连接已重新启动[{}]，URL: {}", eventSourceKey.getDesc(), url);
    }

    private Request.Builder getRequestBuilder(Method method, String url, Map<String, String> headers, String jsonBody) {
        Request.Builder builder = new Request.Builder().url(url);
        if (CollUtil.isNotEmpty(headers)) {
            builder.headers(Headers.of(headers)).header("Accept", "text/event-stream");
        }
        if (method == Method.POST) {
            builder.post(RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8")));
        }
        return builder;
    }

    public void cancel(EventSourceKeyEm eventSourceKey) {
        if (eventSourceMap.containsKey(eventSourceKey)) {
            EventSourceAndListenerObj sourceAndListenerObj = eventSourceMap.get(eventSourceKey);
            sourceAndListenerObj.eventSourceListener().cancel();
            eventSourceMap.remove(eventSourceKey);
        }
    }

    /**
     * 关闭 SSE 连接
     */
    public void close(EventSourceKeyEm eventSourceKey) {
        if (eventSourceMap.containsKey(eventSourceKey)) {
            log.info("正在关闭连接[{}]", eventSourceKey.getDesc());
            EventSourceAndListenerObj sourceAndListenerObj = eventSourceMap.get(eventSourceKey);
            sourceAndListenerObj.eventSourceListener().cancel();
            sourceAndListenerObj.eventSource().cancel();
            eventSourceMap.remove(eventSourceKey);
        }
        // OkHttpClient 的线程池会被关闭，导致后续无法再执行新的请求, 所以不关闭线程池
        /*if (client != null) {
            client.dispatcher().executorService().shutdown();
        }*/
    }

    /**
     * 定时重新连接
     *
     * @param eventSourceKey      事件源的键
     * @param url                 新的 SSE 地址
     * @param eventSourceListener 事件源监听器
     */
    public void scheduleReconnect(EventSourceKeyEm eventSourceKey, String url,
                                  BaseEventSourceListener eventSourceListener) {
        if (retryCount >= MAX_RETRIES) {
            log.error("已达到最大重试次数，停止重连");
            return;
        }

        // 指数退避 + 随机抖动
        long delay = (long) (BASE_RETRY_DELAY_MS * Math.pow(2, retryCount))
                     + (long) (Math.random() * 1000); // 添加随机抖动

        retryCount++;

        log.info("将在 {}ms 后尝试第 {} 次重连...", delay, retryCount);

        new Thread(() -> {
            try {
                Thread.sleep(delay);
                // 重新连接
                start(eventSourceKey, url, eventSourceListener);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

}
