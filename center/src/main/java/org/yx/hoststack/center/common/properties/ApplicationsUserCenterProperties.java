package org.yx.hoststack.center.common.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @Description : Custom Configuration - Volume related Configuration
 * @Author : Lee666
 * @Date : 2025/1/4
 * @Version : 1.0
 */
@RefreshScope
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Configuration
@ToString
@ConfigurationProperties(prefix = "applications.user-center")
public class ApplicationsUserCenterProperties {
    private String ip;

    private String checkAuthTokenUrl;
}