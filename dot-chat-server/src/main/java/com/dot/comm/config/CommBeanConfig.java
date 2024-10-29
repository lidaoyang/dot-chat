package com.dot.comm.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dot.comm.entity.OSSConfig;
import com.dot.comm.filter.LogMDCFilter;
import com.dot.comm.utils.RedisUtil;
import com.dot.msg.chat.config.ChatConfig;
import com.dot.sys.upload.config.UploadFileConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * 公共Bean配置类
 */
@Configuration
public class CommBeanConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    @ConfigurationProperties(prefix = "aliyun.oss")
    public OSSConfig ossConfig() {
        return new OSSConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = "chat")
    public ChatConfig getChatConfig() {
        return new ChatConfig();
    }


    /**
     * 图片上传配置 bean
     */
    @Bean("imageConfig")
    @ConfigurationProperties(prefix = "mat.upload.image")
    public UploadFileConfig getImageConfig() {
        return new UploadFileConfig();
    }

    /**
     * 视频上传配置 bean
     */
    @Bean("videoConfig")
    @ConfigurationProperties(prefix = "mat.upload.video")
    public UploadFileConfig getVideoConfig() {
        return new UploadFileConfig();
    }

    /**
     * 文件上传配置 bean
     */
    @Bean("fileConfig")
    @ConfigurationProperties(prefix = "mat.upload.file")
    public UploadFileConfig getFileConfig() {
        return new UploadFileConfig();
    }

    @Bean(name = "redisUtil")
    public RedisUtil redisUtil(StringRedisTemplate redisTemplate) {
        return new RedisUtil(redisTemplate);
    }

    @Bean(name = "prodRedisUtil")
    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "dev")
    public RedisUtil prodRedisUtil() {
        return new RedisUtil(prodRedisTemplate());
    }

    /**
     * 创建生产环境redis连接, 只有在测试环境下才创建
     *
     * @return
     */
    public StringRedisTemplate prodRedisTemplate() {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(createJedisConnectionFactory(0, "47.98.210.198", 6379, "pinmai.123"));
        setSerializer(redisTemplate);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 公共方法:创建redis连接工厂
     */
    public JedisConnectionFactory createJedisConnectionFactory(int dbIndex, String host, int port, String password) {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        redisStandaloneConfiguration.setDatabase(dbIndex);
        redisStandaloneConfiguration.setPassword(RedisPassword.of(password));
        JedisClientConfiguration.JedisPoolingClientConfigurationBuilder jpcb = (JedisClientConfiguration.JedisPoolingClientConfigurationBuilder) JedisClientConfiguration.builder();
        jpcb.poolConfig(getPoolConfig())
                .and().connectTimeout(Duration.ofMillis(30000));
        JedisClientConfiguration jedisClientConfiguration = jpcb.build();
        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
    }

    /**
     * 公共方法:设置连接池属性
     */
    public JedisPoolConfig getPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMaxTotal(200);
        jedisPoolConfig.setMaxWaitMillis(-1);
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(-1);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestWhileIdle(true);
        return jedisPoolConfig;
    }

    /**
     * 公共方法:设置RedisTemplate的序列化方式
     */
    public void setSerializer(StringRedisTemplate redisTemplate) {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);// key序列化
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);// value序列化
        redisTemplate.setHashKeySerializer(stringSerializer);// Hash key序列化
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);// Hash value序列化
        redisTemplate.afterPropertiesSet();
    }

    @Bean
    public FilterRegistrationBean<LogMDCFilter> logFilterRegistration() {
        FilterRegistrationBean<LogMDCFilter> registration = new FilterRegistrationBean<>();
        // 注入过滤器
        registration.setFilter(new LogMDCFilter());
        // 拦截规则
        registration.addUrlPatterns("/*");
        // 过滤器名称
        registration.setName("logMDCFilter");
        // 过滤器顺序
        registration.setOrder(0);
        return registration;
    }

    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));// 如果配置多个插件,切记分页最后添加
        // interceptor.addInnerInterceptor(new PaginationInnerInterceptor()); 如果有多数据源可以不配具体类型 否则都建议配上具体的DbType
        return interceptor;
    }
}
