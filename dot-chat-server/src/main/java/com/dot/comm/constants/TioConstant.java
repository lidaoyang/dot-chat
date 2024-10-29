package com.dot.comm.constants;

import org.tio.utils.time.Time;

/**
 * token操作常量
 *
 * @author: Meng-meng.
 * @date: Created in 2023/9/26 15:36
 */
public interface TioConstant {

    /**
     * 协议名字(可以随便取，主要用于开发人员辨识)
     */
    String PROTOCOL_NAME = "dot";

    String CHARSET = "utf-8";
    /**
     * 监听的ip
     */
    String SERVER_IP = null;//null表示监听所有，并不指定ip

    /**
     * 监听端口
     */
    int SERVER_PORT = 9326;

    /**
     * 心跳超时时间，单位：毫秒
     */
    int HEARTBEAT_TIMEOUT = 1000 * 60;

    /**
     * ip数据监控统计，时间段
     *
     * @author tanyaowu
     */
    interface IpStatDuration {
        Long DURATION_1 = Time.MINUTE_1 * 5;
        Long[] IPSTAT_DURATIONS = new Long[]{DURATION_1};
    }
}
