package org.yx.hoststack.center.jobs.cmd.volume;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UnMountVolumeCmdData {

    /**
     * hostId: hostId
     * mountInfoList: mountInfoList
     */

    private String hostId;
    private List<UnMountVolumeInfo> unMountInfoList;


    @Getter
    @Setter
    @Builder
    public static class UnMountVolumeInfo {

        /**
         * volumeId : volumeId
         * cid : cid
         * mountType : local/net
         */

        private String volumeId;
        private String cid;
        private String mountType;
    }
}
