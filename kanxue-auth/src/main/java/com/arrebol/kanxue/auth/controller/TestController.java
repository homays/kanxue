package com.arrebol.kanxue.auth.controller;

import com.arrebol.framework.common.response.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试
 *
 * @author Arrebol
 * @date 2024/5/25
 */
@RestController
public class TestController {

    @GetMapping("/test")
    @ApiOperationLog(description = "测试接口")
    public Response<String> test() {
        return Response.success("Hello, 看雪专栏");
    }

}