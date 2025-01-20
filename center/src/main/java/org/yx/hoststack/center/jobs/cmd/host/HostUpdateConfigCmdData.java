package org.yx.hoststack.center.jobs.cmd.host;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class HostUpdateConfigCmdData {
    private List<HostConfig> configs;
    private List<String> hostIds;

    @Getter
    @Setter
    @Builder
    public static class HostConfig {
        private String type;
        private Map<String, String> context;
    }
}
