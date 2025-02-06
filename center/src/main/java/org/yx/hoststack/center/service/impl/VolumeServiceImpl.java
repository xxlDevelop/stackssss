package org.yx.hoststack.center.service.impl;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.dynamic.datasource.annotation.Slave;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.netty.channel.Channel;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.yx.hoststack.center.apiservice.ApiServiceBase;
import org.yx.hoststack.center.common.enums.JobSubTypeEnum;
import org.yx.hoststack.center.common.enums.JobTypeEnum;
import org.yx.hoststack.center.common.exception.VolumeException;
import org.yx.hoststack.center.common.req.channel.SendChannelBasic;
import org.yx.hoststack.center.common.req.volume.*;
import org.yx.hoststack.center.common.resp.PageResp;
import org.yx.hoststack.center.common.resp.volume.*;
import org.yx.hoststack.center.entity.Volume;
import org.yx.hoststack.center.jobs.JobIdGenerator;
import org.yx.hoststack.center.jobs.JobManager;
import org.yx.hoststack.center.jobs.SendJobResult;
import org.yx.hoststack.center.jobs.cmd.JobCmd;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.jobs.cmd.volume.CreateVolumeCmdData;
import org.yx.hoststack.center.jobs.cmd.volume.DeleteVolumeCmdData;
import org.yx.hoststack.center.jobs.cmd.volume.MountVolumeCmdData;
import org.yx.hoststack.center.jobs.cmd.volume.UnMountVolumeCmdData;
import org.yx.hoststack.center.mapper.VolumeMapper;
import org.yx.hoststack.center.service.VolumeService;
import org.yx.hoststack.center.service.center.CenterService;
import org.yx.lib.utils.constant.CommonConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.StringUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.yx.hoststack.center.common.constant.CenterEvent.Action.*;
import static org.yx.hoststack.center.common.constant.CenterEvent.*;
import static org.yx.hoststack.center.common.constant.VolumeConstants.DISK_TYPE_LOCAL;
import static org.yx.hoststack.center.common.constant.VolumeConstants.VOLUME_TYPE_USER;
import static org.yx.hoststack.center.common.enums.SysCode.*;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VolumeServiceImpl extends ServiceImpl<VolumeMapper, Volume> implements VolumeService {


    private final VolumeMapper volumeMapper;
    private final CenterService centerService;
    private final JobManager jobManager;
    private final ApiServiceBase apiServiceBase;
    private final JobIdGenerator jobIdGenerator;
    private final HttpServletRequest request;


    @Override
    public Page<Volume> findPage(Volume params) {
        Page<Volume> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<Volume> query = Wrappers.lambdaQuery(Volume.class);
        return volumeMapper.selectPage(page, query);
    }

    @Override
    public List<Volume> findList(Volume params) {
        LambdaQueryWrapper<Volume> query = Wrappers.lambdaQuery(Volume.class);
        return volumeMapper.selectList(query);
    }

    @Override
    public Volume findById(Long id) {
        return volumeMapper.selectById(id);
    }

    @Override
    public boolean insert(Volume volume) {
        return save(volume);
    }

    @Override
    public boolean update(Volume volume) {
        return updateById(volume);
    }

    @Override
    public int delete(Long id) {
        return volumeMapper.deleteById(id);
    }

    @Override
    public R<?> createVolume(CreateVolumeReq req) {
        try {
            if (req.getVolumeCount() == null) {
                req.setVolumeCount(1);
            }
            if (req.getVolumeSize() == null) {
                req.setVolumeSize(128);
            }
            if (req.getVolumeType() == null) {
                req.setVolumeType(VOLUME_TYPE_USER);
            }
            if (req.getDiskType() == null) {
                req.setDiskType(DISK_TYPE_LOCAL);
            }
            // If no value is passed, all servers are forwarded
            if (StringUtil.isBlank(req.getRelay()) && StringUtil.isBlank(req.getIdc())) {
                //TODO send all servers
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, CREATE_VOLUME_EVENT)
                        .p(LogFieldConstants.ACTION, SEND_ALL_SERVER)
                        .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                        .d();
                return R.ok();
            }
            String serviceId = StringUtil.isNotBlank(req.getIdc()) ? req.getIdc() : req.getRelay();
            Optional<Channel> relayChannel = Optional.ofNullable(centerService.findLocalChannel(SendChannelBasic.builder().serviceId(serviceId).build()));
            if (relayChannel.isPresent()) {
                try {
                    return createVolumeRespR(req);
                } catch (Exception e) {
                    return R.failed(x00000511.getValue(), x00000511.getMsg());
                }
            }
            // post remote
            String postUrl = centerService.buildRemoteUrl(SendChannelBasic.builder().serviceId(serviceId).build(), request.getRequestURI());
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CREATE_VOLUME_EVENT)
                    .p(LogFieldConstants.ACTION, SEND_REMOTE_SERVER)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .p(LogFieldConstants.ReqUrl, postUrl)
                    .d();
            return apiServiceBase.post(postUrl, MDC.get(CommonConstants.TRACE_ID), centerService.prepareRequestHeaders(), JSON.toJSONString(req))
                    .map(result -> JSON.parseObject(result, R.class))
                    .doOnError(e -> logError(e, postUrl, req, CREATE_VOLUME_EVENT, FETCH_CHANNEL_FROM_REMOTE_FAILED))
                    .onErrorReturn(R.failed(x00000506.getValue(), x00000506.getMsg()))
                    .doOnNext(r -> logSuccess(r, postUrl, req, CREATE_VOLUME_EVENT, CREATE_VOLUME_POST_REMOTE_SUCCESS)).block();
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CREATE_VOLUME_EVENT)
                    .p(LogFieldConstants.ACTION, CREATE_VOLUME_FAIL)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .e();
            return R.failed(x00000511.getValue(), x00000511.getMsg());
        }
    }

    public void logError(Throwable e, String remoteUrl, Object req, String event, String action) {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, event)
                .p(LogFieldConstants.ACTION, action)
                .p(LogFieldConstants.ERR_MSG, e.getMessage())
                .p(LogFieldConstants.Alarm, 0)
                .p(LogFieldConstants.API_URL, remoteUrl)
                .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                .p(LogFieldConstants.TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                .e(e);
    }

    public void logSuccess(R<?> r, String remoteUrl, Object req, String event, String action) {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, event)
                .p(LogFieldConstants.ACTION, action)
                .p(LogFieldConstants.API_URL, remoteUrl)
                .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                .p(LogFieldConstants.RespData, JSON.toJSONString(r))
                .p(LogFieldConstants.TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                .i();
    }

    public R<CreateVolumeResp> createVolumeRespR(CreateVolumeReq req) {
        try {
            JobInnerCmd<CreateVolumeCmdData> jobInnerCmd = sendCreateVolumeInstruction(req);
            if (jobInnerCmd != null) {
                R<SendJobResult> sendJobResultR = jobManager.sendJob(jobInnerCmd);
                jobManager.setJobSendResult(sendJobResultR);
                if (sendJobResultR == null) {
                    return R.failed(x00000517.getValue(), x00000517.getMsg());
                }
                if (sendJobResultR.getCode() != R.ok().getCode()) {
                    return R.failed(sendJobResultR.getCode(), sendJobResultR.getMsg());
                }
                return R.ok(CreateVolumeResp.builder().jobId(jobInnerCmd.getJobId()).build());
            } else {
                return R.failed(x00000516.getValue(), x00000516.getMsg());
            }
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CREATE_VOLUME_EVENT)
                    .p(LogFieldConstants.ACTION, CREATE_VOLUME_FAIL)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .e();
            throw new RuntimeException(e.getMessage());
        }
    }

    public Volume buildVolume(CreateVolumeReq req) {
        if (req == null) {
            throw new IllegalArgumentException("CreateVolumeReq cannot be null");
        }
        String volumeId = UUID.fastUUID().toString();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        // Convert GB to KB for volumeSize (1GB = 1024 * 1024 KB)
        long volumeSizeInKB = (req.getVolumeSize() != null ? req.getVolumeSize() : 128) * 1024L * 1024L;
        return Volume.builder()
                .volumeId(volumeId)
                .volumeSize(volumeSizeInKB)
                .diskType(StringUtils.defaultIfBlank(req.getDiskType(), DISK_TYPE_LOCAL))
                .volumeType(StringUtils.defaultIfBlank(req.getVolumeType(), VOLUME_TYPE_USER))
                .downloadUrl(req.getDownloadUrl())
                .createAt(now)
                .build();
    }

    public List<String> generateIds(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        List<String> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(jobIdGenerator.generateJobId());
        }
        return ids;
    }

    public JobInnerCmd<CreateVolumeCmdData> sendCreateVolumeInstruction(CreateVolumeReq req) {
        JobCmd<CreateVolumeCmdData> jobCmd = JobCmd.<CreateVolumeCmdData>builder()
                // Set job type to Volume
                .jobType(JobTypeEnum.VOLUME)
                // Set job subtype to CREATE
                .jobSubType(JobSubTypeEnum.CREATE)
                // Set job data
                .jobData(CreateVolumeCmdData.builder()
                        // Example Set the data volume size
                        .volumeSize(req.getVolumeSize())
                        // Set the data volume type (base/user)
                        .volumeType(req.getVolumeType())
                        // Set disk type (local/net)
                        .diskType(req.getDiskType())
                        // Set md5
                        .md5(req.getMd5())
                        // Set download URL
                        .sourceUrl(req.getDownloadUrl())
                        // Setting the Host ID
                        .hostId(req.getHostId())
                        .snapshotName("")// TODO snapshotName
                        // Example Set the data volume ID list
                        .volumeId(generateIds(req.getVolumeCount()))
                        .build())
                // Set tenant ID
                .tenantId(req.getTenantId())
                .build();

        return jobManager.createJob(jobCmd);

    }

    @Override
    public R<?> deleteVolumes(DeleteVolumeReq req) {
        try {
            // If no value is passed, all servers are forwarded
            if (StringUtil.isBlank(req.getRelay()) && StringUtil.isBlank(req.getIdc())) {
                //TODO send all servers
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, DELETE_VOLUME_EVENT)
                        .p(LogFieldConstants.ACTION, SEND_ALL_SERVER)
                        .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                        .d();
                return R.ok();
            }
            String serviceId = StringUtil.isNotBlank(req.getIdc()) ? req.getIdc() : req.getRelay();
            Optional<Channel> relayChannel = Optional.ofNullable(centerService.findLocalChannel(SendChannelBasic.builder().serviceId(serviceId).build()));
            if (relayChannel.isPresent()) {
                try {
                    return deleteVolumeRespR(req);
                } catch (Exception e) {
                    return R.failed(x00000512.getValue(), x00000512.getMsg());
                }
            }
            // post remote
            String postUrl = centerService.buildRemoteUrl(SendChannelBasic.builder().serviceId(serviceId).build(), request.getRequestURI());
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, DELETE_VOLUME_EVENT)
                    .p(LogFieldConstants.ACTION, SEND_REMOTE_SERVER)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .p(LogFieldConstants.ReqUrl, postUrl)
                    .d();
            return apiServiceBase.post(postUrl, MDC.get(CommonConstants.TRACE_ID), centerService.prepareRequestHeaders(), JSON.toJSONString(req))
                    .map(result -> JSON.parseObject(result, R.class))
                    .doOnError(e -> logError(e, postUrl, req, DELETE_VOLUME_EVENT, FETCH_CHANNEL_FROM_REMOTE_FAILED))
                    .onErrorReturn(R.failed(x00000506.getValue(), x00000506.getMsg()))
                    .doOnNext(r -> logSuccess(r, postUrl, req, DELETE_VOLUME_EVENT, DELETE_VOLUME_POST_REMOTE_SUCCESS)).block();
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, DELETE_VOLUME_EVENT)
                    .p(LogFieldConstants.ACTION, DELETE_VOLUME_FAIL)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .e();
            return R.failed(x00000512.getValue(), x00000512.getMsg());
        }
    }

    public R<DeleteVolumeResp> deleteVolumeRespR(DeleteVolumeReq req) {
        try {
            // Asynchronously send the DELETE volume command to the partition
            JobInnerCmd<DeleteVolumeCmdData> jobInnerCmd = sendDeleteVolumeInstruction(req);
            if (jobInnerCmd != null) {
                R<SendJobResult> sendJobResultR = jobManager.sendJob(jobInnerCmd);
                jobManager.setJobSendResult(sendJobResultR);
                if (sendJobResultR == null) {
                    return R.failed(x00000519.getValue(), x00000519.getMsg());
                }
                if (sendJobResultR.getCode() != R.ok().getCode()) {
                    return R.failed(sendJobResultR.getCode(), sendJobResultR.getMsg());
                }
                return R.ok(DeleteVolumeResp.builder().jobId(jobInnerCmd.getJobId()).build());
            } else {
                return R.failed(x00000520.getValue(), x00000520.getMsg());
            }
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, DELETE_VOLUME_EVENT)
                    .p(LogFieldConstants.ACTION, DELETE_VOLUME_FAIL)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .e();
            // Throw other exceptions
            throw new RuntimeException(e.getMessage());
        }
    }

    public JobInnerCmd<DeleteVolumeCmdData> sendDeleteVolumeInstruction(DeleteVolumeReq req) {
        List<Volume> volumes = lambdaQuery()
                .in(Volume::getVolumeId, req.getVolumeIds())
                .list();
        if (volumes.isEmpty() || volumes.size() != req.getVolumeIds().size()) {
            throw new IllegalArgumentException("Volume not found or volume count does not match");
        }
        Volume firstVolume = volumes.getFirst();

        JobCmd<DeleteVolumeCmdData> jobCmd = JobCmd.<DeleteVolumeCmdData>builder()
                // Set job type to Volume
                .jobType(JobTypeEnum.VOLUME)
                // Set job subtype to DELETE
                .jobSubType(JobSubTypeEnum.DELETE)
                // Set job data
                .jobData(DeleteVolumeCmdData.builder()
                        .volumeType(firstVolume.getVolumeType())
                        .diskType(firstVolume.getDiskType())
                        .hostId("")
                        .volumeIds(req.getVolumeIds())
                        .build())
                // Set target location parameters (optional)
                .tenantId(10000)// TODO x-user get tenantId
                .build();
        return jobManager.createJob(jobCmd);

    }

    @Override
    @Slave
    public R<?> mountVolume(MountVolumeReq req) {
        try {
            // If no value is passed, all servers are forwarded
            if (StringUtil.isBlank(req.getRelay()) && StringUtil.isBlank(req.getIdc())) {
                //TODO send all servers
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, MOUNT_VOLUME_EVENT)
                        .p(LogFieldConstants.ACTION, SEND_ALL_SERVER)
                        .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                        .d();
                return R.ok();
            }
            String serviceId = StringUtil.isNotBlank(req.getIdc()) ? req.getIdc() : req.getRelay();
            Optional<Channel> relayChannel = Optional.ofNullable(centerService.findLocalChannel(SendChannelBasic.builder().serviceId(serviceId).build()));
            if (relayChannel.isPresent()) {
                try {
                    return mountVolumeRespR(req);
                } catch (Exception e) {
                    return R.failed(x00000513.getValue(), x00000513.getMsg());
                }
            }
            // post remote
            String postUrl = centerService.buildRemoteUrl(SendChannelBasic.builder().serviceId(serviceId).build(), request.getRequestURI());
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, MOUNT_VOLUME_EVENT)
                    .p(LogFieldConstants.ACTION, SEND_REMOTE_SERVER)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .p(LogFieldConstants.ReqUrl, postUrl)
                    .d();
            return apiServiceBase.post(postUrl, MDC.get(CommonConstants.TRACE_ID), centerService.prepareRequestHeaders(), JSON.toJSONString(req))
                    .map(result -> JSON.parseObject(result, R.class))
                    .doOnError(e -> logError(e, postUrl, req, MOUNT_VOLUME_EVENT, FETCH_CHANNEL_FROM_REMOTE_FAILED))
                    .onErrorReturn(R.failed(x00000506.getValue(), x00000506.getMsg()))
                    .doOnNext(r -> logSuccess(r, postUrl, req, MOUNT_VOLUME_EVENT, MOUNT_VOLUME_POST_REMOTE_SUCCESS)).block();
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, MOUNT_VOLUME_EVENT)
                    .p(LogFieldConstants.ACTION, MOUNT_VOLUME_FAIL)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .e();
            return R.failed(x00000513.getValue(), x00000513.getMsg());
        }
    }

    public R<MountVolumeResp> mountVolumeRespR(MountVolumeReq req) {
        try {
            // Asynchronously send the MOUNT volume command to the partition
            JobInnerCmd<MountVolumeCmdData> jobInnerCmd = sendMountVolumeInstruction(req);
            if (jobInnerCmd != null) {
                R<SendJobResult> sendJobResultR = jobManager.sendJob(jobInnerCmd);
                jobManager.setJobSendResult(sendJobResultR);
                if (sendJobResultR == null) {
                    return R.failed(x00000521.getValue(), x00000521.getMsg());
                }
                if (sendJobResultR.getCode() != R.ok().getCode()) {
                    return R.failed(sendJobResultR.getCode(), sendJobResultR.getMsg());
                }
                return R.ok(MountVolumeResp.builder().jobId(jobInnerCmd.getJobId()).build());
            } else {
                return R.failed(x00000522.getValue(), x00000522.getMsg());
            }
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, MOUNT_VOLUME_EVENT)
                    .p(LogFieldConstants.ACTION, MOUNT_VOLUME_FAIL)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .e();
            // Throw other exceptions
            throw new RuntimeException(e.getMessage());
        }
    }

    public JobInnerCmd<MountVolumeCmdData> sendMountVolumeInstruction(MountVolumeReq req) {
        List<MountVolumeCmdData.MountVolumeInfo> mountInfoList = volumeMapper.selectMountInfoByCidAndVolumeId(req.getCid(), req.getVolumeId());

        if (CollectionUtils.isEmpty(mountInfoList)) {
            throw new VolumeException(x00000515.getValue(), x00000515.getMsg());
        }

        JobCmd<MountVolumeCmdData> jobCmd = JobCmd.<MountVolumeCmdData>builder()
                // Set job type to Volume
                .jobType(JobTypeEnum.VOLUME)
                // Set job subtype to MOUNT
                .jobSubType(JobSubTypeEnum.MOUNT)
                // Set job data
                .jobData(MountVolumeCmdData.builder()
                        .mountInfoList(mountInfoList)
                        .build())
                // Set tenant ID
                .tenantId(10000)// TODO x-user get tenantId
                .build();
        return jobManager.createJob(jobCmd);

    }

    @Override
    @Slave
    public R<?> unmountVolume(UnmountVolumeReq req) {
        try {
            // If no value is passed, all servers are forwarded
            if (StringUtil.isBlank(req.getRelay()) && StringUtil.isBlank(req.getIdc())) {
                //TODO send all servers
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, UNMOUNT_VOLUME_EVENT)
                        .p(LogFieldConstants.ACTION, SEND_ALL_SERVER)
                        .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                        .d();
                return R.ok();
            }
            String serviceId = StringUtil.isNotBlank(req.getIdc()) ? req.getIdc() : req.getRelay();
            Optional<Channel> relayChannel = Optional.ofNullable(centerService.findLocalChannel(SendChannelBasic.builder().serviceId(serviceId).build()));
            if (relayChannel.isPresent()) {
                try {
                    return unmountVolumeRespR(req);
                } catch (Exception e) {
                    return R.failed(x00000523.getValue(), x00000523.getMsg());
                }
            }
            // post remote
            String postUrl = centerService.buildRemoteUrl(SendChannelBasic.builder().serviceId(serviceId).build(), request.getRequestURI());
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, UNMOUNT_VOLUME_EVENT)
                    .p(LogFieldConstants.ACTION, SEND_REMOTE_SERVER)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .p(LogFieldConstants.ReqUrl, postUrl)
                    .d();
            return apiServiceBase.post(postUrl, MDC.get(CommonConstants.TRACE_ID), centerService.prepareRequestHeaders(), JSON.toJSONString(req))
                    .map(result -> JSON.parseObject(result, R.class))
                    .doOnError(e -> logError(e, postUrl, req, UNMOUNT_VOLUME_EVENT, FETCH_CHANNEL_FROM_REMOTE_FAILED))
                    .onErrorReturn(R.failed(x00000506.getValue(), x00000506.getMsg()))
                    .doOnNext(r -> logSuccess(r, postUrl, req, UNMOUNT_VOLUME_EVENT, UNMOUNT_VOLUME_POST_REMOTE_SUCCESS)).block();
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, UNMOUNT_VOLUME_EVENT)
                    .p(LogFieldConstants.ACTION, UNMOUNT_VOLUME_FAIL)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .e();
            return R.failed(x00000523.getValue(), x00000523.getMsg());
        }
    }

    public R<UnmountVolumeResp> unmountVolumeRespR(UnmountVolumeReq req) {
        try {
            // Asynchronously send the UNMOUNT volume command to the partition
            JobInnerCmd<UnMountVolumeCmdData> jobInnerCmd = sendUnmountVolumeInstruction(req);
            if (jobInnerCmd != null) {
                R<SendJobResult> sendJobResultR = jobManager.sendJob(jobInnerCmd);
                jobManager.setJobSendResult(sendJobResultR);
                if (sendJobResultR == null) {
                    return R.failed(x00000524.getValue(), x00000524.getMsg());
                }
                if (sendJobResultR.getCode() != R.ok().getCode()) {
                    return R.failed(sendJobResultR.getCode(), sendJobResultR.getMsg());
                }
                return R.ok(UnmountVolumeResp.builder().jobId(jobInnerCmd.getJobId()).build());
            } else {
                return R.failed(x00000525.getValue(), x00000525.getMsg());
            }
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, UNMOUNT_VOLUME_EVENT)
                    .p(LogFieldConstants.ACTION, UNMOUNT_VOLUME_FAIL)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .e();
            // Throw other exceptions
            throw new RuntimeException(e.getMessage());
        }
    }

    public JobInnerCmd<UnMountVolumeCmdData> sendUnmountVolumeInstruction(UnmountVolumeReq req) {
        List<UnMountVolumeCmdData.UnMountVolumeInfo> unmountInfoList = volumeMapper.selectUnmountInfoByCidAndVolumeId(req.getCid(), req.getVolumeId());

        if (CollectionUtils.isEmpty(unmountInfoList)) {
            throw new VolumeException(x00000515.getValue(), x00000515.getMsg());
        }

        JobCmd<UnMountVolumeCmdData> jobCmd = JobCmd.<UnMountVolumeCmdData>builder()
                // Set job type to Volume
                .jobType(JobTypeEnum.VOLUME)
                // Set job subtype to UNMOUNT
                .jobSubType(JobSubTypeEnum.UNMOUNT)
                // Set job data
                .jobData(UnMountVolumeCmdData.builder()
                        .unMountInfoList(unmountInfoList)
                        .build())
                // Set tenant ID
                .tenantId(10000)// TODO x-user get tenantId
                .build();
        return jobManager.createJob(jobCmd);

    }

    @Override
    @Slave
    public R<PageResp<VolumeListResp>> listVolumes(VolumeListReq req) {
        try {
            // Create pagination object and execute query
            IPage<VolumeListResp> resultPage = baseMapper.selectVolumeList(Page.of(req.getCurrent(), req.getSize()), req);

            // Convert to response
            PageResp<VolumeListResp> resultData = new PageResp<>();
            resultData.setCurrent(req.getCurrent());
            resultData.setSize(req.getSize());
            resultData.setTotal(resultPage.getTotal());
            resultData.setPages(resultPage.getPages());
            resultData.setRecords(resultPage.getRecords());

            return R.ok(resultData);
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, LIST_VOLUME_EVENT)
                    .p(LogFieldConstants.ACTION, LIST_VOLUME_FAIL)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .e(e);
            return R.failed(x00000526.getValue(), x00000526.getMsg());
        }
    }

    @Override
    public R<PageResp<VolumeMountRelResp>> listVolumeMountRel(VolumeMountRelReq req) {
        try {
            // Execute paginated query
            IPage<VolumeMountRelResp> resultPage = baseMapper.selectVolumeMountRelList(Page.of(req.getCurrent(), req.getSize()), req);

            // Convert to PageResp
            PageResp<VolumeMountRelResp> resultData = new PageResp<>();
            resultData.setCurrent(resultPage.getCurrent());
            resultData.setSize(resultPage.getSize());
            resultData.setTotal(resultPage.getTotal());
            resultData.setPages(resultPage.getPages());
            resultData.setRecords(resultPage.getRecords());

            return R.ok(resultData);

        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, LIST_VOLUME_MOUNT_REL_EVENT)
                    .p(LogFieldConstants.ACTION, LIST_VOLUME_MOUNT_REL_FAIL)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .e(e);
            return R.failed(x00000526.getValue(), x00000526.getMsg());
        }
    }

}