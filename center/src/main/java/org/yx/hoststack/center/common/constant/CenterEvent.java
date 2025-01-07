package org.yx.hoststack.center.common.constant;

public interface CenterEvent {
    String CenterWsServer = "CenterWsServer";
    String ENCRYPTABLE_PROPERTY_RESOLVER_EVENT = "EncryptablePropertyResolverEvent";
    String IDC_NET_CONFIG_SAVE_EVENT = "IDC_NET_CONFIG_SAVE_EVENT";
    String TOKEN_FILTER_EVENT = "TokenFilterEvent";
    String THREAD_TASK_REJECTED_EVENT = "ThreadTaskRejectedEVENT";
    String RELAY_EVENT = "RelayEvent";

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
        String CenterWsServer_HostInitialize = "HostInitialize";
        String CenterWsServer_Region_Initialize = "RegionInitialize";


        String Update_IdcInfo_Failed = "Update_IdcInfo_Failed";
        String IDC_NET_CONFIG_SAVE_FAILED = "IDC_NET_CONFIG_SAVE_FAILED";

        String TOKEN_FILTER_EVENT_ACTION_INIT = "TokenFilterInit";
        String TOKEN_FILTER_EVENT_ACTION_DO_FILTER = "TokenFilterDoFilter";
        String TASK_REJECTED = "TaskRejected";

        String RELAY_EVENT_ACTION_RELAY_UPDATE = "RelayEventActionRelayUpdate";
    }

}
