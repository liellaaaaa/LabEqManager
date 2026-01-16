package org.cong.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器配置
 * 当前阶段使用 NoOpPasswordEncoder 支持明文密码，便于开发测试
 * 后续生产环境需要改为 BCryptPasswordEncoder
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 暂时使用明文密码，不进行加密
        return NoOpPasswordEncoder.getInstance();
    }
}


