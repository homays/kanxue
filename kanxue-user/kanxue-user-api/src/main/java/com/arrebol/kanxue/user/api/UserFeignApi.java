package com.arrebol.kanxue.user.api;

import com.arrebol.framework.common.response.Response;
import com.arrebol.kanxue.user.contant.ApiConstants;
import com.arrebol.kanxue.user.dto.req.FindUserByPhoneReqDTO;
import com.arrebol.kanxue.user.dto.req.RegisterUserReqDTO;
import com.arrebol.kanxue.user.dto.req.UpdateUserPasswordReqDTO;
import com.arrebol.kanxue.user.dto.resp.FindUserByPhoneRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface UserFeignApi {

    String PREFIX = "/user";

    @PostMapping(value = PREFIX + "/register")
    Response<Long> registerUser(@RequestBody RegisterUserReqDTO registerUserReqDTO);

    @PostMapping(value = PREFIX + "/findByPhone")
    Response<FindUserByPhoneRspDTO> findByPhone(FindUserByPhoneReqDTO findUserByPhoneReqDTO);

    @PostMapping(value = PREFIX + "/password/update")
    Response<?> updatePassword(@RequestBody UpdateUserPasswordReqDTO updateUserPasswordReqDTO);

}