package org.yx.hoststack.common.syscode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EdgeSysCode {
    Success("Success", 0),
    SendAgentFailByChannelNotActive("SendAgentFailByChannelNotActive", 121001),
    SendAgentFailByLimit("SendAgentFailByLimit", 121002),
    UnknownJob("UnknownJob", 121003),
    NotFoundAgentSession("NotFoundAgentSession", 121004),
    UpstreamServiceNotAvailable("UpstreamServiceNotAvailable", 121401),
    PortoParseException("PortoParseException", 121500),
    DoJobException("DoJobException", 121501),
    ;
    private final String msg;
    private final int value;
}