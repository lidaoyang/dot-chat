package com.dot.sse;

import com.dot.sse.util.SseEmitterUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.function.Consumer;

/**
 * 自定义SSE对象
 *
 * @author: Dao-yang.
 * @date: Created in 2025/4/1 16:23
 */
@Slf4j
@Getter
public class MySseEmitter extends SseEmitter {

    private final SseStateListener listener;

    private boolean cancelled = false;

    public MySseEmitter(Long timeout, SseStateListener listener) {
        super(timeout);
        this.listener = listener;
    }

    public void cancel() {
        super.complete();
        cancelled = true;
    }

    /**
     * SSE 连接完成 回调（连接已关闭，正准备释放）
     * 触发条件(前提: 客户端的连接没断开)
     * 1. 调用 complete()
     * 2. 调用 completeWithError(e)
     * 3. 超时断开连接（例如：new SseEmitter(1000L)，那么在 1 秒后就会调用 onCompletion() 的回调函数）；回调调用顺序：onTimeout() -> onCompletion()
     */
    public void oncomplete(Integer userId) {
        super.onCompletion(completionCallBack(userId));
    }

    /**
     * SSE 连接超时
     */
    public void ontimeout(Integer userId) {
        super.onTimeout(timeoutCallBack(userId));
    }

    /**
     * SSE 异常回调
     * 指定当发生错误时执行的回调方法。这个错误可能是由于网络连接问题等原因。
     */
    public void onerror(Integer userId) {
        super.onError(errorCallBack(userId));
    }

    public interface SseStateListener {
       default void onCompletion(){
           log.debug("连接关闭-调用监听默认实现！");
       };

        default void onTimeout(){
            log.debug("连接超时-调用监听默认实现！");
        };

       default void onError(Throwable e){
           log.debug("连接异常-调用监听默认实现！");
       };
    }


    private Runnable completionCallBack(Integer userId) {
        return () -> {
            log.info("连接已关闭，准备释放！userId:{}", userId);
            // 将 sse 连接 移除
            SseEmitterUtil.remove(userId);
            // 完成回调
            listener.onCompletion();
        };
    }

    private Runnable timeoutCallBack(Integer userId) {
        return () -> {
            log.info("连接超时，准备释放！userId:{}", userId);
            SseEmitterUtil.closeSSE(userId);
            // 超时回调
            listener.onTimeout();
        };
    }

    private Consumer<Throwable> errorCallBack(Integer userId) {
        return e -> {
            log.debug("连接异常,userId:{}", userId, e);
            SseEmitterUtil.closeSSE(userId);
            // 错误回调
            listener.onError(e);
        };
    }
}
