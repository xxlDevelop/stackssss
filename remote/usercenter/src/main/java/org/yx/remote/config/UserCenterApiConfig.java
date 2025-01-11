package org.yx.remote.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhang
 */
@Data
@RefreshScope
@Configuration
public class UserCenterApiConfig {

    @Value("${uc.api.auth-check-token-url}")
    private String authCheckTokenUrl;
}
