package org.yx.hoststack.center.common;

public interface CenterEvent {
    String Center_WS_SERVER = "CenterWsServer";

    interface Action {
        String REFRESH_SERVICE_DETAIL_DB = "RefreshServiceDetailDB";
        String SERVICE_REGISTER = "ServiceRegister";
        String INVALID_IP = "InvalidIPFailed";
        String SEND_MSG_SUCCESS = "sendMsgSuccess";
        String SEND_MSG_FAILED = "SendMsgFailed";
    }

}
