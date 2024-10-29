package com.dot.msg.chat.tio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.tio.server.TioServerConfig;
import org.tio.utils.time.Time;

/**
 * Tio服务配置
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/10 19:00
 */
@Data
@Component
@ConfigurationProperties(prefix = "tio.server")
public class JRTioConfig {

    /**
     * 监听端口
     */
    private int port = 9326;
    /**
     * 监听的ip null表示监听所有，并不指定ip
     */
    private String serverIp = null;

    /**
     * 协议名字(可以随便取，主要用于开发人员辨识)
     */
    private String protocolName = "dot";

    private String charset = "utf-8";

    /**
     * 心跳超时时间，单位：毫秒
     */
    private int heartbeatTimeout = 1000 * 60;

    /**
     * 如果你希望通过wss来访问(不通过nginx转发),到配置中开启,不过首先你得有SSL证书（证书必须和域名相匹配，否则可能访问不了ssl）
     */
    private SslConfig ssl;

    /**
     * Tio服务配置
     */
    private TioServerConfig tioConfig;

    @Data
    public static class SslConfig {
        /**
         * 是否开启ssl
         */
        private boolean enable;

        /**
         * ssl证书文件
         */
        private String keyStore;

        private String trustStore;

        /**
         * ssl证书密码
         */
        private String pwd;

    }

    /**
     * ip数据监控统计，时间段
     */
    public final Long[] IPSTAT_DURATIONS = new Long[]{Time.MINUTE_1 * 5};
}
