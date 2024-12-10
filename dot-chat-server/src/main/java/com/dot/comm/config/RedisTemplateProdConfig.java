package com.dot.comm.config;

import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;


/**
 * Redis 线上环境对象
 *
 * @author: Dao-yang.
 * @date: Created in 2024/11/6 15:36
 */
public class RedisTemplateProdConfig {

    /**
     * 创建生产环境redis连接, 只有在测试环境下才创建
     *
     * @return StringRedisTemplate
     */
    public StringRedisTemplate prodRedisTemplate() {
        LettuceConnectionFactory connectionFactory = createLettuceConnectionFactory(0, System.getenv("DB_HOST"), 6379, System.getenv("REDIS_PASSWORD"));
        connectionFactory.afterPropertiesSet();
        StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory);
        // 使用默认的序列化方式
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 公共方法:创建redis连接工厂
     */
    public LettuceConnectionFactory createLettuceConnectionFactory(int dbIndex, String host, int port,
                                                                   String password) {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        redisStandaloneConfiguration.setDatabase(dbIndex);
        redisStandaloneConfiguration.setPassword(RedisPassword.of(password));
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }
}

