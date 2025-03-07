package com.dot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.dot.chat", "com.dot.sys", "com.dot.deepseek", "com.dot.comm.config", "com.dot.comm.manager", "com.dot.comm.exception"})
@MapperScan(basePackages = {"com.dot.chat.dao", "com.dot.deepseek.dao", "com.dot.sys.auth.dao"})
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class DotChatAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(DotChatAdminApplication.class, args);
    }
}
