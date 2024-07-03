package com.arrebol.kanxue.oss.biz.service.impl;

import com.arrebol.framework.common.response.Response;
import com.arrebol.kanxue.oss.biz.service.FileService;
import com.arrebol.kanxue.oss.biz.strategy.FileStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Resource
    private FileStrategy fileStrategy;

    @Override
    public Response<?> uploadFile(MultipartFile file) {
        // 上传文件到
        String url = fileStrategy.uploadFile(file, "kanxue");

        return Response.success(url);
    }
}