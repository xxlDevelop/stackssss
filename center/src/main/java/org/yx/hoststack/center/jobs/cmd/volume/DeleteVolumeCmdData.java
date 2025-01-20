package org.yx.hoststack.center.jobs.cmd.volume;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class DeleteVolumeCmdData {

    /**
     * volumeType : base/user
     * diskType : local/net
     * hostId : hostId
     * volumeIds: volumeIds
     */
    private String volumeType;
    private String diskType;
    private String hostId;

    private List<String> volumeIds;
}
