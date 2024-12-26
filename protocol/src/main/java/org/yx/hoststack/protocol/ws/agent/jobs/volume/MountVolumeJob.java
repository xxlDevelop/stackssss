package org.yx.hoststack.protocol.ws.agent.jobs.volume;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MountVolumeJob {
    private String volumeId;
    private String baseVolumeId;
    private String cid;
    private String mountType;
}
