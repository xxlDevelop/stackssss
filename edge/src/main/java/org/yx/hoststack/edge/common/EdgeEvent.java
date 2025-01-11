package org.yx.hoststack.edge.common;

public interface EdgeEvent {
    String EDGE_WS_CLIENT = "EdgeWsClient";
    String EDGE_WS_SERVER = "EdgeWsServer";
    String BUSINESS = "Business";
    String WORK_QUEUE_CONSUMER = "WorkQueueConsumer";
    String FORWARDING_PROTOCOL = "ForwardingProtocol";
    String JOB = "Job";
    String JOB_NOTIFY_TO_FILE = "JobNotifyToFile";

    interface Action {
        String CHANNEL_ACTIVE = "ChannelActive";
        String CHANNEL_INACTIVE = "ChannelInactive";
        String HANDLER_REMOVED = "HandlerRemoved";
        String SEND_MSG_SUCCESSFUL = "SendMsgSuccessful";
        String SEND_MSG_FAILED = "SendMsgFailed";
        String RE_SEND_MSG_FAILED = "ReSendMsgFailed";
        String FORWARDING_MSG_FAILED = "ForwardingMsgFailed";
        String RE_SEND_MSG_FAILED_LIMIT = "ReSendMsgFailedLimit";
        String RE_SEND_MSG_SUCCESSFUL = "ReSendMsgSuccessful";
        String STATIS = "Statis";
        String RECEIVE_MSG = "ReceiveMsg";
        String PROCESS_MSG = "ProcessMsg";
        String CLEAR_TEMP_CONTEXT_MAPPING = "ClearTempContextMapping";
        String FORWARDING_NODE_DESTROY = "ForwardingNodeDestroy";
        String PROCESS_JOB = "ProcessJob";

        String START_INIT = "StartInit";
        String INIT_ERROR = "InitError";
        String CONNECT_ERROR = "ConnectError";
        String CONNECT_SUCCESSFUL = "ConnectSuccessful";
        String PREPARE_DESTROY = "PrepareDestroy";
        String DESTROY_SUCCESSFUL = "DestroySuccessful";
        String HANDSHAKE_SUCCESSFUL = "HandshakeSuccessful";
        String HANDSHAKE_TIMEOUT = "HandshakeTimeout";
        String CLOSE_BY_SERVER = "CloseByServer";


        String CENTER_PONG = "CenterPong";
        String EDGE_REGISTER_SUCCESSFUL = "EdgeRegisterSuccessful";
        String EDGE_REGISTER_FAILED = "EdgeRegisterFailed";
        String EDGE_CONFIG_SYNC = "EdgeConfigSync";
        String REGION_CONFIG_SYNC = "RegionConfigSync";
        String PREPARE_HANDSHAKE = "PrepareHandshake";
        String X_TOKEN_VALID = "X_TOKEN_VALID";
        String HOST_ACTIVE_CLOSE = "HostActiveClose";
        String CREATE_HOST_SESSION = "CreateHostSession";
        String CREATE_FORWARDING_NODE = "CreateForwardingNode";
        String NOT_FOUND_HOST_SESSION = "NotFoundHostSession";
        String HOST_INITIALIZE = "HostInitialize";
        String HOST_INITIALIZE_SUCCESSFUL = "HostInitializeSuccessful";
        String HOST_INITIALIZE_FAILED = "HostInitializeFailed";

        String JOB_FIND_TARGET_SESSION = "JobFindTargetSession";
        String AGENT_JOB_NOTIFY = "AgentJobNotify";
        String FORWARDING_TO_IDC = "ForwardingToIdc";
        String FORWARDING_TO_IDC_FAIL = "ForwardingToIdcFail";
        String FORWARDING_TO_CENTER = "ForwardingToCenter";
        String FORWARDING_TO_CENTER_FAIL = "ForwardingToCenterFail";
        String FORWARDING_NODE_PING = "ForwardingNodePing";
        String IDC_EXIT = "IdcExit";

        String CONSUMER_HOST_HB = "ConsumerHostHb";
        String CONSUMER_NOT_SEND_JOB_NOTIFY = "ConsumerNotSendJobNotify";
        String CONSUMER_HOST_EXIT = "ConsumerHostExit";
    }

}
