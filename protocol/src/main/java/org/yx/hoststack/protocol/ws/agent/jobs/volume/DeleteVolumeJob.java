package org.yx.hoststack.protocol.ws.agent.jobs.volume;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteVolumeJob {
    private String volumeId;
}
