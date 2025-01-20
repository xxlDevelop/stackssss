package org.yx.hoststack.center.jobs.cmd.container;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CtrlContainerCmdData {
    private String hostId;
    private String ctrl;
    private List<String> cIds;
}
