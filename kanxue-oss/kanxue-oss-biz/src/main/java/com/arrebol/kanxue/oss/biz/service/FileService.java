package com.arrebol.kanxue.oss.biz.service;

import com.arrebol.framework.common.response.Response;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * 上传文件
     */
    Response<?> uploadFile(MultipartFile file);
}