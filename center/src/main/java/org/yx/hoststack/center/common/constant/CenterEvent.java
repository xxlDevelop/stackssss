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
    String STORAGE_IDC_EVENT = "STORAGE_IDC_EVENT";
    String STORAGE_FILE_EVENT = "STORAGE_FILE_EVENT";
    String SEND_MSG_TO_LOCAL_OR_REMOTE_CHANNEL_EVENT = "SendMsgToLocalOrRemoteChannelEvent";
    String JOB_CONTROLLER_EVENT = "JobControllerEvent";
    String CREATE_IMAGE_EVENT = "CreateImageEvent";
    String CREATE_VOLUME_EVENT = "CreateVolumeEvent";
    String DELETE_VOLUME_EVENT = "DeleteVolumeEvent";
    String MOUNT_VOLUME_EVENT = "MountVolumeEvent";
    String UNMOUNT_VOLUME_EVENT = "UnmountVolumeEvent";
    String LIST_VOLUME_EVENT = "ListVolumeEvent";
    String LIST_VOLUME_MOUNT_REL_EVENT = "ListVolumeMountRelEvent";
    String OSS_CONFIG_SERVICE_IMPL_EVENT = "OssConfigServiceImplEvent";

    String INIT_CACHE = "InitCache";

    String CREATE_JOB = "CreateJob";
    String SEND_JOB = "SendJob";
    String PROCESS_JOB_NOTIFY = "ProcessJobNotify";

    String CONTAINER_EVENT = "Container";
    String HOST_RESET_EVENT = "HostReset";

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
        String SEND_ALL_SERVER = "SendAllServer";
        String SEND_REMOTE_SERVER = "SendRemoteServer";
        String SYNC_CONFIG_TO_IDC_FAILED = "SyncConfigToIdcFailed";
        String STORAGE_IDC_CREATE_BUCKET_FAILED = "StorageIdcCreateBucketFailed";
        String STORAGE_IDC_DELETE_BUCKET_FAILED = "StorageIdcDeleteBucketFailed";
        String STORAGE_IDC_LIST_BUCKETS_FAILED = "StorageIdcListBucketsFailed";
        String STORAGE_FILE_LIST_FAILED = "StorageFileListFailed";

        String FETCH_CHANNEL_FROM_REMOTE_SUCCESS = "FetchChannelFromRemoteSuccess";
        String CREATE_IMAGE_FAIL = "CreateImageFail";
        String CREATE_VOLUME_FAIL = "CreateVolumeFail";
        String DELETE_VOLUME_FAIL = "DeleteVolumeFail";
        String MOUNT_VOLUME_FAIL = "MountVolumeFail";
        String UNMOUNT_VOLUME_FAIL = "UnmountVolumeFail";
        String CREATE_IMAGE_POST_REMOTE_SUCCESS = "CreateImagePostRemoteSuccess";
        String CREATE_VOLUME_POST_REMOTE_SUCCESS = "CreateVolumePostRemoteSuccess";
        String DELETE_VOLUME_POST_REMOTE_SUCCESS = "DeleteVolumePostRemoteSuccess";
        String MOUNT_VOLUME_POST_REMOTE_SUCCESS = "MountVolumePostRemoteSuccess";
        String UNMOUNT_VOLUME_POST_REMOTE_SUCCESS = "UnmountVolumePostRemoteSuccess";
        String DUPLICATE_IMAGE_FOUNT = "DuplicateImageFound";
        String GET_OSS_CONFIG_BY_REGION_FAIL = "GetOssConfigByRegionFail";
        String S3_GENERATE_UPLOAD_URL_FAIL = "S3GenerateUploadUrl";


        String Update_IdcInfo_Failed = "UpdateIdcInfoFailed";
        String IDC_NET_CONFIG_SAVE_FAILED = "IdcNetConfigSaveFailed";
        String Query_Relay_Failed = "QueryRelayFailed";
        String LIST_VOLUME_FAIL = "ListVolumeFail";
        String LIST_VOLUME_MOUNT_REL_FAIL = "ListVolumeMountRelFail";

        String TOKEN_FILTER_EVENT_ACTION_INIT = "TokenFilterInit";
        String TOKEN_FILTER_EVENT_ACTION_DO_FILTER = "TokenFilterDoFilter";
        String TASK_REJECTED = "TaskRejected";

        String RELAY_EVENT_ACTION_RELAY_UPDATE = "RelayEventActionRelayUpdate";

        String SEND_JOB_TO_EDGE = "SendJobToEdge";
        String SEND_JOB_NEXT_JOB = "SendNextJob";

        String JOB_NOTIFY = "JobNotify";

        String PERSISTENCE_TO_DB = "PersistenceToDb";

        String CREATE_IMAGE = "CreateImage";
        String DELETE_IMAGE_DOWNLOAD_INF = "DeleteImageDownloadInf";
        String DELETE_IMAGE = "DeleteImage";
        String CREATE_VOLUME = "CreateVolume";
        String MOUNT_VOLUME = "MountVolume";
        String UN_MOUNT_VOLUME = "UnMountVolume";
    }

}
