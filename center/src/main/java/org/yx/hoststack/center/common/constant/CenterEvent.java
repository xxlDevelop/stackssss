package org.yx.hoststack.center.common.constant;

public interface CenterEvent {
    String CenterWsServer = "CenterWsServer";
    String ENCRYPTABLE_PROPERTY_RESOLVER_EVENT = "EncryptablePropertyResolverEvent";
    String IDC_NET_CONFIG_SAVE_EVENT = "IdcNetConfigSaveEvent";
    String TOKEN_FILTER_EVENT = "TokenFilterEvent";
    String THREAD_TASK_REJECTED_EVENT = "ThreadTaskRejectedEvent";
    String RELAY_EVENT = "RelayEvent";
    String FIND_LOCAL_CHANNEL_EVENT = "FindLocalChannelEvent";
    String SEND_MSG_TO_CHANNEL_EVENT = "SendMsgToChannelEvent";
    String FETCH_CHANNEL_FROM_REMOTE_EVENT = "FetchChannelFromRemoteEvent";
    String SYNC_CONFIG_TO_IDC_EVENT = "SYNC_CONFIG_TO_IDC_EVENT";
    String SEND_MSG_TO_LOCAL_OR_REMOTE_CHANNEL_EVENT = "SendMsgToLocalOrRemoteChannelEvent";

    String DoJob = "DoJob";

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
        String FIND_LOCAL_CHANNEL_FAILED = "RegionInitializeFailed";
        String SEND_MSG_TO_CHANNEL_FAILED = "SendMsgToChannelFailed";
        String SEND_MSG_TO_CHANNEL_SUCCESSFULLY = "SendMsgToChannelSuccessfully";
        String FETCH_CHANNEL_FROM_REMOTE_INIT = "FetchChannelFromRemoteInit";
        String SEND_MSG_TO_LOCAL_OR_REMOTE_CHANNEL_REMOTE = "SendMsgToLocalOrRemoteChannelRemote";
        String FETCH_CHANNEL_FROM_REMOTE_FAILED = "FetchChannelFromRemoteFailed";
        String SYNC_CONFIG_TO_IDC_FAILED = "SyncConfigToIdcFailed";
        String FETCH_CHANNEL_FROM_REMOTE_SUCCESS = "FetchChannelFromRemoteSuccess";


        String Update_IdcInfo_Failed = "UpdateIdcInfoFailed";
        String IDC_NET_CONFIG_SAVE_FAILED = "IdcNetConfigSaveFailed";
        String Query_Relay_Failed = "QueryRelayFailed";

        String TOKEN_FILTER_EVENT_ACTION_INIT = "TokenFilterInit";
        String TOKEN_FILTER_EVENT_ACTION_DO_FILTER = "TokenFilterDoFilter";
        String TASK_REJECTED = "TaskRejected";

        String RELAY_EVENT_ACTION_RELAY_UPDATE = "RelayEventActionRelayUpdate";
    }

}
