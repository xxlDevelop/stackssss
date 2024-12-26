package org.yx.hoststack.protocol.ws.agent.jobs.host;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpgradeVolumeJob {
    private String originVolumeId;
    private String newVolumeId;
    private boolean keepOrigin;
}
