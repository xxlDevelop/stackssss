package org.yx.hoststack.protocol.ws.agent.req;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class HostHeartbeatReq {

    /**
     * vmStatus : [{"vmName":"vm1","imageVer":"1.0","cid":"container123","running":true}]
     * hostStatus : {"cpuUsage":85,"memoryUsage":75}
     */

    private HostStatus hostStatus;
    private List<VmStatus> vmStatus;

    @NoArgsConstructor
    @Data
    public static class HostStatus {
        /**
         * cpuUsage : 85
         * memoryUsage : 75
         */

        private int cpuUsage;
        private int memoryUsage;
    }

    @NoArgsConstructor
    @Data
    public static class VmStatus {
        /**
         * vmName : vm1
         * imageVer : 1.0
         * cid : container123
         * working : true
         */

        private String vmName;
        private String imageVer;
        private String cid;
        private boolean running;
    }
}
