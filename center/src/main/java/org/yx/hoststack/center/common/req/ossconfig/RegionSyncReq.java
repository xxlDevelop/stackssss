package org.yx.hoststack.center.common.req.ossconfig;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegionSyncReq {

    @NotNull(message = "Region cannot be null")
    private String region;
    private Config config;


    @Data
    public static class Config {
        @NotNull(message = "OSS configuration cannot be null")
        private Config oss;

        @NotNull(message = "RTC configuration cannot be null")
        private Config rtc;
    }
}
