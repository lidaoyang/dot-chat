package com.dot.comm.config;

import com.dot.comm.interceptor.AccessLimitInterceptor;
import com.dot.comm.interceptor.AdminAuthInterceptor;
import com.dot.comm.interceptor.AdminTokenInterceptor;
import com.dot.comm.filter.LogMDCFilter;
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
    public HandlerInterceptor adminTokenInterceptor() {
        return new AdminTokenInterceptor();
    }

    @Bean
    public HandlerInterceptor adminAuthInterceptor() {
        return new AdminAuthInterceptor();
    }

    @Bean
    public HandlerInterceptor accessLimitInterceptor() {
        return new AccessLimitInterceptor();
    }

    /**
     * 不需要拦截的静态资源
     */
    final String[] excludeStaticPathPatterns = {"/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**"};

    /**
     * 不需要拦截的接口
     */
    final String[] excludeApiPathPatterns = {"/api/sys/auth/admin/login"};
    /**
     * 不需要校验菜单权限的接口
     */
    final String[] excludeApiPathPatterns2 = {"/api/sys/auth/role/menu/list"};

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加token拦截器
        // addPathPatterns添加需要拦截的命名空间；
        // excludePathPatterns添加排除拦截命名空间
        // 限流限制拦截器
        registry.addInterceptor(accessLimitInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(excludeStaticPathPatterns);

        // 用户登录token 拦截器
        registry.addInterceptor(adminTokenInterceptor())
                .addPathPatterns("/api/sys/**", "/api/chat/**")
                .excludePathPatterns(excludeStaticPathPatterns)
                .excludePathPatterns(excludeApiPathPatterns);

        // 用户菜单权限拦截器
        registry.addInterceptor(adminAuthInterceptor())
                .addPathPatterns("/api/sys/**", "/api/chat/**")
                .excludePathPatterns(excludeStaticPathPatterns)
                .excludePathPatterns(excludeApiPathPatterns2)
                .excludePathPatterns(excludeApiPathPatterns);


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
