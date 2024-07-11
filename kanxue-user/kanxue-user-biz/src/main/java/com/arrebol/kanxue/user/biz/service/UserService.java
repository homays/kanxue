package com.arrebol.kanxue.user.biz.service;

import com.arrebol.framework.common.response.Response;
import com.arrebol.kanxue.user.biz.model.vo.UpdateUserInfoReqVO;
import com.arrebol.kanxue.user.dto.req.FindUserByPhoneReqDTO;
import com.arrebol.kanxue.user.dto.req.RegisterUserReqDTO;
import com.arrebol.kanxue.user.dto.req.UpdateUserPasswordReqDTO;
import com.arrebol.kanxue.user.dto.resp.FindUserByPhoneRspDTO;

public interface UserService {

    /**
     * 更新用户信息
     */
    Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO);

    /**
     * 用户注册
     */
    Response<Long> register(RegisterUserReqDTO registerUserReqDTO);

    /**
     * 根据电话获取用户信息
     */
    Response<FindUserByPhoneRspDTO> findByPhone(FindUserByPhoneReqDTO findUserByPhoneReqDTO);

    /**
     * 修改密码
     */
    Response<?> updatePassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO);

}