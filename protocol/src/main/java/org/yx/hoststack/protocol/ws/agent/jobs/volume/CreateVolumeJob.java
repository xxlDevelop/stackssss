package org.yx.hoststack.protocol.ws.agent.jobs.volume;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateVolumeJob {
    private String volumeId;
    private int volumeSize;
    private String diskType;
    private String volumeType;
    private String sourceUrl;
}
