package com.arrebol.kanxue.auth.controller;

import com.arrebol.framework.biz.operationlog.aspect.ApiOperationLog;
import com.arrebol.framework.common.response.Response;
import com.arrebol.kanxue.auth.model.vo.user.UserLoginReqVO;
import com.arrebol.kanxue.auth.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    @ApiOperationLog(description = "用户登录/注册")
    public Response<String> loginAndRegister(@Validated @RequestBody UserLoginReqVO userLoginReqVO) {
        return userService.loginAndRegister(userLoginReqVO);
    }

    @PostMapping("/logout")
    @ApiOperationLog(description = "用户登出")
    public Response<?> logout(@RequestHeader("userId") String userId) {
        return userService.logout(Long.valueOf(userId));
    }

}