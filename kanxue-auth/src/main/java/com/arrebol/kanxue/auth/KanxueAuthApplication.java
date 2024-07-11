package com.arrebol.kanxue.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.arrebol.kanxue.auth.domain.mapper")
@EnableFeignClients(basePackages = "com.arrebol.kanxue")
public class KanxueAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(KanxueAuthApplication.class, args);
    }

}
