package com.arrebol.kanxue.auth.service;

import com.arrebol.framework.common.response.Response;
import com.arrebol.kanxue.auth.model.vo.verificationcode.SendVerificationCodeReqVO;

public interface VerificationCodeService {

    /**
     * 发送短信验证码
     */
    Response<?> send(SendVerificationCodeReqVO sendVerificationCodeReqVO);

}