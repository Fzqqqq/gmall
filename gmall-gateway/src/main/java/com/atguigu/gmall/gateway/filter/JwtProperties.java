package com.atguigu.gmall.gateway.filter;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

/**
 * @author fzqqq
 * @create 2020-10-19 18:26
 */
@Data
@Slf4j
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {
    private String pubKeyPath;
    private PublicKey publicKey;
    private String cookieName;

    @PostConstruct
    public void init() {
        try {
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥失败", e);
            throw new RuntimeException();
        }
    }

}
