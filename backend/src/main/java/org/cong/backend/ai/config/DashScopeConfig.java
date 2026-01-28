package org.cong.backend.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeConfig {

    /**
     * DashScope API Key
     */
    private String apiKey;

    /**
     * 模型名称，默认为 qwen-turbo
     */
    private String model = "qwen-turbo";
}

