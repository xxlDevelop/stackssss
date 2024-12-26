package org.yx.hoststack.protocol.ws.agent.jobs.host;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class HostUpdateConfigJob {


    private List<Config> config;

    @Data
    @Builder
    public static class Config {
        /**
         * type : base
         * context : {"key":"value"}
         */

        private String type;
        private Map<String, String> context;
    }
}
