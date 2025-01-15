package org.yx.hoststack.common.syscode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EdgeSysCode {
    Success("Success", 0),
    CloseByServer("CloseByServer", 121000),
    SendAgentFailByChannelNotActive("SendAgentFailByChannelNotActive", 121001),
    IdcNotReady("Idc not ready", 121002),
    RelayNotReady("Relay not ready", 121003),
    LinkSideError("LinkSide not match, Not ServerToClient", 121004),
    SendAgentFailByLimit("SendAgentFailByLimit", 121005),
    UnknownJob("UnknownJob", 121006),
    NotFoundAgentSession("NotFoundAgentSession", 121007),
    HttpCallFailed("HttpCallFailed", 121008),
    SendMsgFailed("SendMsgFailed", 121009),
    UpstreamServiceNotAvailable("UpstreamServiceNotAvailable", 121401),
    DownloadStreamServiceNotAvailable("DownloadStreamServiceNotAvailable", 121402),
    Exception("Exception", 121500),
    PortoParseException("PortoParseException", 121501),
    DoJobException("DoJobException", 121502),
    ;
    private final String msg;
    private final int value;
}