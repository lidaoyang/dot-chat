package com.jr.comm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 访问限制注解
 *
 * @author: Dao-yang.
 * @date: Created in 2022/12/20 10:16
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccessLimit {
    
    /**
     * 秒值
     **/
    int seconds() default 1;
    
    /**
     * 秒值中最大访问次数
     **/
    int maxCount() default 6;
}
