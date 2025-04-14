package com.dot.sse;

import com.dot.sse.listener.BaseEventSourceListener;
import okhttp3.sse.EventSource;

/**
 * EventSource和Listener封装对象
 *
 * @author: Dao-yang.
 * @date: Created in 2025/3/28 17:09
 */
public record EventSourceAndListenerObj(EventSource eventSource, BaseEventSourceListener eventSourceListener) {
}
