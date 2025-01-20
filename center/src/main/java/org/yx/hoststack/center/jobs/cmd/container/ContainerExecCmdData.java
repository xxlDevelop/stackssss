package org.yx.hoststack.center.jobs.cmd.container;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ContainerExecCmdData {
    private String script;
    private List<String> cIds;
}
