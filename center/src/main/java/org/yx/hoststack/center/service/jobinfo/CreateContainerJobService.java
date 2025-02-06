package org.yx.hoststack.center.service.jobinfo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.yx.hoststack.center.common.enums.*;
import org.yx.hoststack.center.common.events.publister.ContainerCreateJobEventPublisher;
import org.yx.hoststack.center.common.properties.ApplicationsVolumeProperties;
import org.yx.hoststack.center.common.req.container.ContainerCreateReqDTO;
import org.yx.hoststack.center.common.req.job.JobDetailContainerStartDTO;
import org.yx.hoststack.center.common.req.job.JobDetailCreateContainerDTO;
import org.yx.hoststack.center.common.req.job.JobDetailCreateVolumeDTO;
import org.yx.hoststack.center.common.req.job.JobDetailMountVolumeDTO;
import org.yx.hoststack.center.common.resp.comtainer.ContainerCreateRespVO;
import org.yx.hoststack.center.common.utils.ContainerIdUtil;
import org.yx.hoststack.center.common.utils.JobDetailIdUtil;
import org.yx.hoststack.center.common.utils.UUIDUtils;
import org.yx.hoststack.center.entity.*;
import org.yx.hoststack.center.mapper.JobDetailMapper;
import org.yx.hoststack.center.mapper.JobInfoMapper;
import org.yx.hoststack.center.service.ContainerService;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.yx.hoststack.center.common.constant.ContainerConstants.CONTAINER_ID_FORMAT;

/**
 * @Description : create container jobInfo and jobDetails service
 * @Author : Lee666
 * @Date : 2025/1/4
 * @Version : 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CreateContainerJobService {
    private final JobInfoMapper jobInfoMapper;
    private final JobDetailMapper jobDetailMapper;
    private final ContainerService containerService;
    private final ApplicationsVolumeProperties applicationsVolumeProperties;
    private final ContainerCreateJobEventPublisher containerCreateJobEventPublisher;

    /**
     * insert container create jobs
     *
     * @param tenantId     tenant id
     * @param dto          request data
     * @param imageInfo    image data
     * @param host         host
     * @param containerIps available ips
     * @return root jobId
     */
    @Transactional
    public ContainerCreateRespVO insertContainerCreateJobs(Long tenantId, ContainerCreateReqDTO dto, ImageInfo imageInfo, Host host, List<String> containerIps) {
        Timestamp currentTimestamp = Timestamp.from(Instant.now(Clock.systemUTC()));
        // 解析数据并通过请求参数读取镜像、模板等信息创建job任务链（count - 标识job_detail的条数，每一个job_detail任务只创建一个container）
        final List<JobInfo> jobInfos = new LinkedList<>();
        final List<JobDetail> jobDetails = new LinkedList<>();
        // 封装父job以关联本次创建的job流程任务链,job_info 在设计中为5步的job流程任务链执行顺序固定
        final String parentJobInfoId = UUIDUtils.fastSimpleUUID();
        createParentJobInfo(tenantId, dto, host, jobInfos, parentJobInfoId, currentTimestamp);
        // 获取指定host下container 的最大的 sequenceNumber
        Optional<Container> containerOptional = containerService.getOneOpt(Wrappers.<Container>lambdaQuery().eq(Container::getHostId, dto.getHostId()).orderByDesc(Container::getSequenceNumber)
                .last("limit 1"));
        int sequenceNumber = containerOptional.isPresent() ? containerOptional.get().getSequenceNumber() : 0;
        // 根据请求的入参count创建子任务
        for (int i = 1; i <= dto.getCount(); i++) {
            final String createContainerJobId = UUIDUtils.fastSimpleUUID();
            jobInfos.add(JobInfo.builder()
                    .nextJobId(parentJobInfoId)
                    .jobId(createContainerJobId)
                    .runOrder(1)
                    .jobStatus(JobStatusEnum.WAIT.getName())
                    .jobType(JobTypeEnum.CONTAINER.getName())
                    .jobSubType(JobSubTypeEnum.CREATE.getName())
                    .jobProgress(0)
                    .jobDetailNum(dto.getCount() * 5)
                    .tenantId(tenantId)
                    .externalParams(JSONUtil.toJsonStr(dto))
                    .runTime(0L)
                    .createAt(currentTimestamp)
                    .lastUpdateAt(currentTimestamp)
                    .build());
            String baseVolumeId = null;
            String userVolumeId = null;
            // 1. 创建base_volume任务， 当数据卷为传入的数据卷ID时，不需要创建base_volume，跳过该任务
            if (!StringUtils.hasLength(dto.getBaseVolumeId())) {


                baseVolumeId = UUIDUtils.fastSimpleUUID();
                jobDetails.add(JobDetail.builder()
                        .jobId(createContainerJobId)
                        .jobDetailId(JobDetailIdUtil.getContainerId(createContainerJobId, baseVolumeId))
                        .jobStatus(JobDetailStatusEnum.WAIT.getName())
                        .jobParams(JSONUtil.toJsonStr(JobDetailCreateVolumeDTO.builder()
                                .volumeId(baseVolumeId)
                                .volumeType(VolumeTypeEnum.BASE_VOLUME.getName())
                                .volumeSize(applicationsVolumeProperties.getDefaultSize())
                                .build()))
                        .jobHost(dto.getHostId())
                        .runTime(0)
                        .createAt(currentTimestamp)
                        .lastUpdateAt(currentTimestamp)
                        .build());
            } else {
                baseVolumeId = UUIDUtils.fastSimpleUUID();
            }
            // 2. 创建user_volume任务， 当数据卷为传入的数据卷ID时，不需要创建user_volume，跳过该任务
            if (!StringUtils.hasLength(dto.getUserVolumeId())) {
                userVolumeId = UUIDUtils.fastSimpleUUID();
                jobDetails.add(JobDetail.builder()
                        .jobId(createContainerJobId)
                        .jobDetailId(JobDetailIdUtil.getContainerId(createContainerJobId, userVolumeId))
                        .jobStatus(JobDetailStatusEnum.WAIT.getName())
                        .jobParams(JSONUtil.toJsonStr(JobDetailCreateVolumeDTO.builder()
                                .volumeId(userVolumeId)
                                .volumeType(VolumeTypeEnum.USER_VOLUME.getName())
                                .volumeSize(applicationsVolumeProperties.getDefaultSize())
                                .build()))
                        .jobHost(dto.getHostId())
                        .runTime(0)
                        .createAt(currentTimestamp)
                        .lastUpdateAt(currentTimestamp)
                        .build());
            } else {
                userVolumeId = UUIDUtils.fastSimpleUUID();
            }
            // 3. 创建容器任务

            // new container sequence number
            sequenceNumber += 1;
            String cid = String.format(CONTAINER_ID_FORMAT, host.getHostId(), sequenceNumber);
            jobDetails.add(JobDetail.builder()
                    .jobId(createContainerJobId)
                    .jobDetailId(JobDetailIdUtil.getContainerId(createContainerJobId, ContainerIdUtil.getContainerId(host.getHostId(), sequenceNumber)))
                    .jobStatus(JobDetailStatusEnum.WAIT.getName())
                    .jobParams(JSONUtil.toJsonStr(JobDetailCreateContainerDTO.builder()
                            .containerId(cid)
                            .hostId(dto.getHostId())
                            .sequenceNumber(sequenceNumber)
                            .containerIp(containerIps.get(i - 1))
                            .imageId(imageInfo.getImageId())
                            .imageVer(imageInfo.getImageVer())
                            .vCpu(dto.getProfile().getVCpu())
                            .memory(dto.getProfile().getMemory())
                            .osType(dto.getProfile().getOsType())
                            .netMode(dto.getProfile().getNetMode())
                            .build()))
                    .jobHost(dto.getHostId())
                    .runTime(0)
                    .createAt(currentTimestamp)
                    .lastUpdateAt(currentTimestamp)
                    .build());

            // 4. 创建挂在数据卷任务
            jobDetails.add(JobDetail.builder()
                    .jobId(createContainerJobId)
                    .jobDetailId(JobDetailIdUtil.getContainerId(createContainerJobId, UUIDUtils.fastSimpleUUID()))
                    .jobStatus(JobDetailStatusEnum.WAIT.getName())
                    .jobParams(JSONUtil.toJsonStr(JobDetailMountVolumeDTO.builder()
                            .baseVolumeId(baseVolumeId)
                            .volumeId(userVolumeId)
                            .mountContainerId(cid)
                            .hostId(host.getHostId())
                            .build()))
                    .jobHost(dto.getHostId())
                    .runTime(0)
                    .createAt(currentTimestamp)
                    .lastUpdateAt(currentTimestamp)
                    .build());
            // 5. 创建启动容器任务
            jobDetails.add(JobDetail.builder()
                    .jobId(createContainerJobId)
                    .jobDetailId(JobDetailIdUtil.getContainerId(createContainerJobId, UUIDUtils.fastSimpleUUID()))
                    .jobStatus(JobDetailStatusEnum.WAIT.getName())
                    .jobParams(JSONUtil.toJsonStr(JobDetailContainerStartDTO.builder()
                            .cid(cid)
                            .hostId(host.getHostId())
                            .build()))
                    .jobHost(dto.getHostId())
                    .runTime(0)
                    .createAt(currentTimestamp)
                    .lastUpdateAt(currentTimestamp)
                    .build());
        }

        // save data to db
        jobInfoMapper.insert(jobInfos);
        jobDetailMapper.insert(jobDetails);

        containerCreateJobEventPublisher.publish(parentJobInfoId);

        return ContainerCreateRespVO.builder().jobId(parentJobInfoId).build();
    }

    /**
     * create parent job
     *
     * @param tenantId         tenant id
     * @param dto              req data
     * @param host             host
     * @param jobInfos         new job info list
     * @param parentJobInfoId  parent job info id
     * @param currentTimestamp current timestamp
     */
    private static void createParentJobInfo(Long tenantId, ContainerCreateReqDTO dto, Host host, List<JobInfo> jobInfos, String parentJobInfoId, Timestamp currentTimestamp) {
        jobInfos.add(JobInfo.builder()
                .nextJobId(null)
                .jobId(parentJobInfoId)
                .jobStatus(JobStatusEnum.WAIT.getName())
                .runOrder(0)
                .jobType(JobTypeEnum.CONTAINER.getName())
                .jobSubType(JobSubTypeEnum.CREATE.getName())
                .jobProgress(0)
                .jobDetailNum(0)
                .tenantId(tenantId)
                .externalParams(JSONUtil.toJsonStr(dto))
                .runTime(0L)
                .createAt(currentTimestamp)
                .lastUpdateAt(currentTimestamp)
                .build());
    }

}
