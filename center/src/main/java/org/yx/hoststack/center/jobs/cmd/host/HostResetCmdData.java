package org.yx.hoststack.center.jobs.cmd.host;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class HostResetCmdData {
    private List<String> hostIds;
}
