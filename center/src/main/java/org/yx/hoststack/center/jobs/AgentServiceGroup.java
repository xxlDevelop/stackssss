package org.yx.hoststack.center.jobs;

import lombok.Builder;
import lombok.Getter;
import org.yx.hoststack.center.common.dto.ServiceDetailDTO;

import java.util.List;

@Builder
@Getter
public class AgentServiceGroup {

    private ServiceDetailDTO serviceDetail;
    private List<String> agentIds;
}
