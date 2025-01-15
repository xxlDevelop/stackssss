package org.yx.hoststack.protocol.ws.agent.req;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
public class HostInitializeReq {

    /**
     * agentStartTs : 1234567890
     * agentType : host
     * resourcePool : IDC
     * runtimeEnv: docker
     * osStartTs : 1234567890
     * devSn : device12345
     * osType : linux
     * osVersion : Ubuntu 20.04 LTS
     * agentVersion : 1.0.0
     * osMem : 16384
     * localIp : 192.168.1.1
     * cpuSpec : {"cpuNum":8,"cpuType":"Intel(R) Core(TM) i5-12500H","cpuManufacturer":"Intel","cpuArchitecture":"x86_64","cpuCores":"4","cpuThreads":"8","cpuBaseSpeed":"2.50"}
     * storage : 10000 SSD | 20000 HDD
     * gpuList : [{"gpuType":"NVIDIA GeForce RTX 3080","gpuManufacturer":"NVIDIA","gpuMem":"10","gpuBusType":"PCIe","gpuDeviceId":"GPU12345","gpuBusId":"0000:17:00.0"}]
     * netcardList : [{"netcardName":"eth0","netcardType":"Ethernet","netcardLinkSpeed":"1000"}]
     */
    private long agentStartTs;
    private String agentType;
    private String resourcePool;
    private String runtimeEnv;
    private long osStartTs;
    private String devSn;
    private String osType;
    private String osVersion;
    private String agentVersion;
    private int osMem;
    private String localIp;
    private HostInitializeReq.CpuInfo cpuSpec;
    private String disk;
    private String detailedId;
    private int proxy;
    private List<HostInitializeReq.GpuInfo> gpuList;
    private List<HostInitializeReq.NetCardInfo> netcardList;

    @NoArgsConstructor
    @Data
    public static class CpuInfo {
        /**
         * cpuNum : 8
         * cpuType : Intel(R) Core(TM) i5-12500H
         * cpuManufacturer : Intel
         * cpuArchitecture : x86_64
         * cpuCores : 4
         * cpuThreads : 8
         * cpuBaseSpeed : 2.50
         */

        private int cpuNum;
        private String cpuType;
        private String cpuManufacturer;
        private String cpuArchitecture;
        private int cpuCores;
        private int cpuThreads;
        private float cpuBaseSpeed;
    }

    @NoArgsConstructor
    @Data
    public static class GpuInfo {
        /**
         * gpuType : NVIDIA GeForce RTX 3080
         * gpuManufacturer : NVIDIA
         * gpuMem : 10
         * gpuBusType : PCIe
         * gpuDeviceId : GPU12345
         * gpuBusId : 0000:17:00.0
         */

        private String gpuType;
        private String gpuManufacturer;
        private int gpuMem;
        private String gpuBusType;
        private String gpuDeviceId;
        private String gpuBusId;
    }

    @NoArgsConstructor
    @Data
    public static class NetCardInfo {
        /**
         * netcardName : eth0
         * netcardType : Ethernet
         * netcardLinkSpeed : 1000
         */

        private String netcardName;
        private String netcardType;
        private int netcardLinkSpeed;
    }
}
