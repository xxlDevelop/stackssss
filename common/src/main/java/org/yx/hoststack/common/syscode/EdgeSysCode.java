package org.yx.hoststack.common.syscode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EdgeSysCode {
    Success("Success", 0),
    SendAgentFailByChannelNotActive("SendAgentFailByChannelNotActive", 121001),
    IdcSidMismatched("IdcSid not mismatched", 121002),
    RelaySidMismatched("RelaySid not mismatched", 121002),
    LinkSideError("LinkSide not match, Not ServerToClient", 121003),
    SendAgentFailByLimit("SendAgentFailByLimit", 121004),
    UnknownJob("UnknownJob", 121005),
    NotFoundAgentSession("NotFoundAgentSession", 121006),
    HttpCallFailed("HttpCallFailed", 121007),
    SendMsgFailed("SendMsgFailed", 121008),
    UpstreamServiceNotAvailable("UpstreamServiceNotAvailable", 121401),
    DownloadStreamServiceNotAvailable("DownloadStreamServiceNotAvailable", 121402),
    Exception("Exception", 121500),
    PortoParseException("PortoParseException", 121501),
    DoJobException("DoJobException", 121502),
    ;
    private final String msg;
    private final int value;
}