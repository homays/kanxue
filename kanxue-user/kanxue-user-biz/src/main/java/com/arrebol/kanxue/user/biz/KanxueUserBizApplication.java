package com.arrebol.kanxue.user.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.arrebol.kanxue.user.biz.domain.mapper")
@EnableFeignClients(basePackages = "com.arrebol.kanxue")
public class KanxueUserBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(KanxueUserBizApplication.class, args);
    }
}