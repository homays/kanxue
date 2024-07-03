package com.arrebol.kanxue.auth.service;

import com.arrebol.framework.common.response.Response;
import com.arrebol.kanxue.auth.model.vo.user.UpdatePasswordReqVO;
import com.arrebol.kanxue.auth.model.vo.user.UserLoginReqVO;

public interface UserService {

    /**
     * 登录与注册
     */
    Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO);

    /**
     * 退出登录
     */
    Response<?> logout();

    /**
     * 修改密码
     */
    Response<?> updatePassword(UpdatePasswordReqVO updatePasswordReqVO);
}