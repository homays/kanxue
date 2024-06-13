package com.arrebol.kanxue.auth.controller;

import com.arrebol.kanxue.auth.alarm.AlarmInterface;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {

    @Resource
    private AlarmInterface alarm;

    @GetMapping("/alarm")
    public String sendAlarm() {
        alarm.send("系统出错啦，犬小哈这个月绩效没了，速度上线解决问题！");
        return "alarm success";
    }

}