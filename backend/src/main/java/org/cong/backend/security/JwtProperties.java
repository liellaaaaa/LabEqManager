package org.cong.backend.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * 签名密钥
     */
    private String secret;

    /**
     * 过期时间（秒）
     */
    private long expirationSeconds;
}


