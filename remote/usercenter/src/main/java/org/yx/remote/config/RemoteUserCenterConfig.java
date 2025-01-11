package org.yx.remote.config;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.client.RestTemplate;
import org.yx.lib.utils.rest.RestTemplateConfig;
import org.yx.remote.UserCenterRemoteTenantInfoService;

@Configuration
public class RemoteUserCenterConfig {

    @Bean
    @DependsOn(RestTemplateConfig.YX_REST_TEMPLATE)
    public UserCenterRemoteTenantInfoService remoteTenantInfoService(
            @Qualifier(RestTemplateConfig.YX_REST_TEMPLATE) RestTemplate restTemplate,
            UserCenterApiConfig userCenterApiConfig) {
        return new UserCenterRemoteTenantInfoService(restTemplate, userCenterApiConfig);
    }
}
