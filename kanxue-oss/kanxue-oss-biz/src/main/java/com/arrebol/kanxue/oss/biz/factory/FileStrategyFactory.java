package com.arrebol.kanxue.oss.biz.factory;

import cn.hutool.core.util.StrUtil;
import com.arrebol.framework.common.exception.BizException;
import com.arrebol.kanxue.oss.biz.strategy.FileStrategy;
import com.arrebol.kanxue.oss.biz.strategy.impl.AliyunOSSFileStrategy;
import com.arrebol.kanxue.oss.biz.strategy.impl.MinioFileStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工厂类型
 *
 * @author Arrebol
 * @date 2024/7/3
 */
@Configuration
public class FileStrategyFactory {

    @Value("${strategy.type}")
    private String strategyType;

    @Bean
    public FileStrategy getFileStrategy() {
        if (StrUtil.equals(strategyType, "minio")) {
            return new MinioFileStrategy();
        } else if (StrUtil.equals(strategyType, "aliyun")) {
            return new AliyunOSSFileStrategy();
        }

        throw new IllegalArgumentException("无可用的存储类型");
    }

}
