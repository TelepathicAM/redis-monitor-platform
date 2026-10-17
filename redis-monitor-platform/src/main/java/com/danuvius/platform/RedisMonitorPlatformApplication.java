package com.danuvius.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@MapperScan("com.danuvius.platform.mapper")
public class RedisMonitorPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisMonitorPlatformApplication.class, args);
    }
}
