package org.cong.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * 当前阶段为方便接口联调，临时关闭 Spring Security 自动配置。
 * 等登录与用户模块接口全部验证通过后，再移除 exclude，恢复基于 JWT 的安全控制。
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}

