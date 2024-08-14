package com.arrebol.kanxue.kv.api;

import com.arrebol.framework.common.response.Response;
import com.arrebol.kanxue.kv.constant.ApiConstants;
import com.arrebol.kanxue.kv.req.AddNoteContentReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface KeyValueFeignApi {

    String PREFIX = "/kv";

    @PostMapping(value = PREFIX + "/note/content/add")
    Response<?> addNoteContent(@RequestBody AddNoteContentReqDTO addNoteContentReqDTO);

}