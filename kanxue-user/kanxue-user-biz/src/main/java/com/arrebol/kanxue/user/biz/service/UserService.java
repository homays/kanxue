package com.arrebol.kanxue.user.biz.service;

import com.arrebol.framework.common.response.Response;
import com.arrebol.kanxue.user.biz.model.vo.UpdateUserInfoReqVO;

public interface UserService {

    /**
     * 更新用户信息
     */
    Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO);
}