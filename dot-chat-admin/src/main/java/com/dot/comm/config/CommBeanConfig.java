package com.dot.comm.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.dot.comm.entity.OSSConfig;
import com.dot.comm.utils.RedisUtil;
import com.dot.sys.upload.config.UploadFileConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.client.RestTemplate;

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

    /**
     * 图片上传配置 bean
     */
    @Bean("imageConfig")
    @ConfigurationProperties(prefix = "mat.upload.image")
    public UploadFileConfig getImageConfig() {
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

/*    @Bean(name = "prodRedisUtil")
    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "dev")
    public RedisUtil prodRedisUtil() {
        return new RedisUtil(new RedisTemplateProdConfig().getTemplate());
    }*/

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
