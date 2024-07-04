package com.arrebol.kanxue.user.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.arrebol.kanxue.user.biz.domain.mapper")
public class KanxueUserBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(KanxueUserBizApplication.class, args);
    }
}
