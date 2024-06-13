package com.arrebol.kanxue.auth.alarm;

public interface AlarmInterface {

    /**
     * 发送告警信息
     */
    boolean send(String message);
}