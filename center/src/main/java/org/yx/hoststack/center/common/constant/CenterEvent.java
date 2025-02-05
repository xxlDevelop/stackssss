package org.yx.hoststack.center.common.constant;

public interface CenterEvent {
    String CenterWsServer = "CenterWsServer";
    String ENCRYPTABLE_PROPERTY_RESOLVER_EVENT = "EncryptablePropertyResolverEvent";

    public interface Action {
        String CenterWsServer_StartInit = "StartInit";
        String CenterWsServer_InitError = "InitError";
        String CenterWsServer_ConnectError = "ConnectError";
        String CenterWsServer_ConnectSuccessfully = "ConnectSuccessfully";
        String CenterWsServer_PrepareDestroy = "PrepareDestroy";
        String CenterWsServer_DestroySuccessfully = "DestroySuccessfully";
        String CenterWsServer_HandshakeSuccessfully = "HandshakeSuccessfully";
        String CenterWsServer_HandshakeTimeout = "HandshakeTimeout";
        String CenterWsServer_CloseByServer = "CloseByServer";
        String CenterWsServer_SendMsgSuccessfully = "SendMsgSuccessfully";
        String CenterWsServer_SendMsgFailed = "SendMsgFailed";
        String CenterWsServer_ReSendMsgFailed = "ReSendMsgFailed";
        String CenterWsServer_ReSendMsgSuccessfully = "ReSendMsgSuccessfully";
        String CenterWsServer_ReceiveMsg = "ReceiveMsg";
        String CenterWsServer_EdgeRegisterCenter = "RegisterCenter";


        String Update_IdcInfo_Failed = "Update_IdcInfo_Failed";
    }

}
