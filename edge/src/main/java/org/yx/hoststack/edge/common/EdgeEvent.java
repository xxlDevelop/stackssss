package org.yx.hoststack.edge.common;

public interface EdgeEvent {
    String EdgeWsClient = "EdgeWsClient";
    String EdgeWsServer = "EdgeWsServer";
    String Business = "Business";
    String WorkQueueConsumer = "WorkQueueConsumer";
    String TransferProtocol = "TransferProtocol";
    String Job = "Job";
    String JobNotifyToFile = "JobNotifyToFile";

    interface Action {
        String Channel_Active = "ChannelActive";
        String Channel_Inactive = "ChannelInactive";
        String HANDLER_REMOVED = "HandlerRemoved";
        String SendMsgSuccessful = "SendMsgSuccessful";
        String SendMsgFailed = "SendMsgFailed";
        String ReSendMsgFailed = "ReSendMsgFailed";
        String ReSendMsgFailedLimit = "ReSendMsgFailedLimit";
        String ReSendMsgSuccessful = "ReSendMsgSuccessful";
        String Statis = "Statis";
        String ReceiveMsg = "ReceiveMsg";
        String ProcessMsg = "ProcessMsg";
        String ClearTempContextMapping = "ClearTempContextMapping";
        String TransferNodeDestroy = "TransferNodeDestroy";
        String ProcessJob = "ProcessJob";

        // EdgeWsClient
        String EdgeWsClient_StartInit = "StartInit";
        String EdgeWsClient_InitError = "InitError";
        String EdgeWsClient_ConnectError = "ConnectError";
        String EdgeWsClient_ConnectSuccessful = "ConnectSuccessful";
        String EdgeWsClient_PrepareDestroy = "PrepareDestroy";
        String EdgeWsClient_DestroySuccessful = "DestroySuccessful";
        String EdgeWsClient_HandshakeSuccessful = "HandshakeSuccessful";
        String EdgeWsClient_HandshakeTimeout = "HandshakeTimeout";
        String EdgeWsClient_CloseByServer = "CloseByServer";
        String EdgeWsClient_ReConnectToServer = "ReConnectToServer";

        // business
        String CenterPont = "CenterPont";
        String EdgeRegisterSuccessful = "EdgeRegisterSuccessful";
        String EdgeRegisterFailed = "EdgeRegisterFailed";
        String EdgeConfigSync= "EdgeConfigSync";
        String RegionConfigSync= "RegionConfigSync";
        String PrepareHandshake = "PrepareHandshake";
        String HandshakeSuccessful = "HandshakeSuccessful";
        String HostActiveClose = "HostActiveClose";
        String CreateHostSession = "CreateHostSession";
        String CreateBmkSession = "CreateBmkSession";
        String CreateIdcServiceSession = "CreateTransferNode";
        String NotFoundHostSession = "NotFoundHostSession";
        String HostPrepareInitialize = "HostPrepareInitialize";
        String HostInitializeSuccessful = "HostInitializeSuccessful";
        String HostInitializeFailed = "HostInitializeFailed";

        String JobFindTargetSession = "JobFindTargetSession";
        String AgentJobNotify = "AgentJobNotify";
        String TransferToIdc = "TransferToIdc";
        String TransferToCenter = "TransferToCenter";

        String WorkQueueConsumer_ConsumerHostHb = "ConsumerHostHb";
        String WorkQueueConsumer_ConsumerNotSendJobNotify = "ConsumerNotSendJobNotify";
        String WorkQueueConsumer_ConsumerHostExit = "ConsumerHostExit";
    }

}
