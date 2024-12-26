package org.yx.hoststack.protocol.ws.agent.jobs.container;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class CtrlContainerJob {

    private String oper;
    private List<String> cids;
}
