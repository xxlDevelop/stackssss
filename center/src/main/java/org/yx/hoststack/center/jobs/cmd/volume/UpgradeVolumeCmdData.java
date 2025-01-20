package org.yx.hoststack.center.jobs.cmd.volume;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
public class UpgradeVolumeCmdData {

    /**
     * hostId: hostId
     * sourceUrl: sourceUrl
     * md5: md5
     * upgradeInfoList: upgradeInfoList
     */

    private String hostId;
    private String sourceUrl;
    private String md5;
    private List<UpgradeVolumeInfo> upgradeInfoList;

    @Getter
    @Setter
    @Builder
    public static class UpgradeVolumeInfo {

        /**
         * originVolumeId : originVolumeId
         * newVolumeId : newVolumeId
         * keepOrigin : true
         */

        private String originVolumeId;
        private String newVolumeId;
        private boolean keepOrigin;
    }
}
