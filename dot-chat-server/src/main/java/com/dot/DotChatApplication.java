package com.dot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.dot.msg", "com.dot.sys", "com.dot.comm.config", "com.dot.comm.manager", "com.dot.comm.exception"})
@MapperScan(basePackages = {"com.dot.msg.*.dao"})
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class DotChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(DotChatApplication.class, args);
    }
}
