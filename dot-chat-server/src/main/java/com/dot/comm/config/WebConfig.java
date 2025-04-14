package com.dot.comm.config;

import com.dot.comm.filter.FrontTokenInterceptor;
import com.dot.comm.filter.LogMDCFilter;
import com.dot.comm.interceptor.AccessLimitInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * token验证拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 这里使用一个Bean为的是可以在拦截器中自由注入，也可以在拦截器中使用SpringUtil.getBean 获取
    // 但是觉得这样更优雅

    @Bean
    public HandlerInterceptor frontTokenInterceptor() {
        return new FrontTokenInterceptor();
    }

    @Bean
    public HandlerInterceptor accessLimitInterceptor() {
        return new AccessLimitInterceptor();
    }

    final String[] excludePathPatterns = {"/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**"};

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加token拦截器
        // addPathPatterns添加需要拦截的命名空间；
        // excludePathPatterns添加排除拦截命名空间
        // 限流限制拦截器
        registry.addInterceptor(accessLimitInterceptor()).addPathPatterns("/**")
                .excludePathPatterns(excludePathPatterns);

        // 前端用户登录token
        registry.addInterceptor(frontTokenInterceptor()).addPathPatterns("/api/sys/**","/api/msg/**")
                .excludePathPatterns(
                        "/api/sys/user/login",
                        "/api/sys/user/registerAndLogin"
                )
                .excludePathPatterns(excludePathPatterns);

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
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
}
