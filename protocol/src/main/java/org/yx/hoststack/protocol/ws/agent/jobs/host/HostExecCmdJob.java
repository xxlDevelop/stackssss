package org.yx.hoststack.protocol.ws.agent.jobs.host;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HostExecCmdJob {

    /**
     * script : # Python script content here print('Hello, World!')
     */
    private String script;
}
