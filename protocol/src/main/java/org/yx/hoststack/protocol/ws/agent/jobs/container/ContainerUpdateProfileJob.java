package org.yx.hoststack.protocol.ws.agent.jobs.container;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ContainerUpdateProfileJob {
    private List<ContainerUpdateProfileJob.Container> containerList;
    @Data
    @Builder
    public static class Container {
        /**
         * profile : jsonProfile
         */

        private String profile;
    }
}
