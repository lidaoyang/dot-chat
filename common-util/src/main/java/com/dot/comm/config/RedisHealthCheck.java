package com.dot.comm.config;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Redis健康检查
 *
 * @author: Dao-yang.
 * @date: Created in 2024/12/13 14:10
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.data.redis.health-check", havingValue = "true")
public class RedisHealthCheck {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 每分钟执行一次
    @Scheduled(fixedRate = 60000)
    public void healthCheck() {
        try {
            // 执行Redis PING命令，检测连接是否活跃
            String result = Objects.requireNonNull(stringRedisTemplate.getConnectionFactory()).getConnection().ping();
            log.debug("Redis PING Response: {}", result);
        } catch (Exception e) {
            // 处理Redis连接异常
            log.error("Redis heartbeat failed", e);
        }
    }
}