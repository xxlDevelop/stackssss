package org.yx.hoststack.center.jobs.cmd.volume;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
public class MountVolumeCmdData {

    /**
     * hostId: hostId
     * mountInfoList: mountInfoList
     */

    private String hostId;
    private List<MountVolumeInfo> mountInfoList;


    @Getter
    @Setter
    @Builder
    public static class MountVolumeInfo {

        /**
         * volumeId : volumeId
         * baseVolumeId : baseVolumeId
         * cid : cid
         * mountType : local/net
         */

        private String volumeId;
        private String baseVolumeId;
        private String cid;
        private String mountType;
    }
}
