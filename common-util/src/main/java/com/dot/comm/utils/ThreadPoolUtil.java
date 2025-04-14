package com.dot.comm.utils;


import cn.hutool.core.thread.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @author: Dao-yang.
 * @date: Created in 2023/7/31 10:38
 */
public class ThreadPoolUtil {

    /**
     * 创建线程池
     *
     * @param poolName        线程池名称
     * @param corePoolSize    核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime   线程池中的空闲线程的存活时间(毫秒)
     * @param workQueue       线程池队列 如果任务量比较大或不确定，建议使用 LinkedBlockingQueue ；如果任务量比较稳定且可预测，可以考虑使用 ArrayBlockingQueue 。
     * @return ExecutorService
     */
    public static ExecutorService createPool(String poolName, int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                             BlockingQueue<Runnable> workQueue) {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNamePrefix(poolName).build();
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, workQueue,
                namedThreadFactory);
    }

    /**
     * 创建单线程定时任务线程池
     *
     * @param poolName 线程池名称
     * @return ScheduledExecutorService
     */
    public static ScheduledExecutorService createSingleThreadScheduled(String poolName) {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNamePrefix(poolName).build();
        return Executors.newSingleThreadScheduledExecutor(namedThreadFactory);
    }
}
