package org.yx.hoststack.center.jobs;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.enums.JobStatusEnum;
import org.yx.hoststack.center.common.enums.JobSubTypeEnum;
import org.yx.hoststack.center.common.enums.JobTypeEnum;
import org.yx.hoststack.center.common.enums.SysCode;
import org.yx.hoststack.center.entity.JobDetail;
import org.yx.hoststack.center.entity.JobInfo;
import org.yx.hoststack.center.jobs.cmd.JobCmd;
import org.yx.hoststack.center.jobs.cmd.JobCmdChain;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.queue.message.JobReportMessage;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.StringUtil;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Service
public class JobManager {
    private final Map<String, IJob> jobMap;
    private final TransactionTemplate transactionTemplate;
    private final JobIdGenerator jobIdGenerator;
    private final JobProcessService jobProcessService;

    public JobManager(Map<String, IJob> jobMap, TransactionTemplate transactionTemplate,
                      JobIdGenerator jobIdGenerator, JobProcessService jobProcessService) {
        this.jobMap = jobMap;
        this.transactionTemplate = transactionTemplate;
        this.jobIdGenerator = jobIdGenerator;
        this.jobProcessService = jobProcessService;
    }

    /**
     *
     * @param jobCmd    jobCmd
     * @return jobId
     * @param <T>   jobData
     *  host:
     *      reset:                          {@link org.yx.hoststack.center.jobs.cmd.host.HostResetCmdData}
     *      updateConfig:                   {@link org.yx.hoststack.center.jobs.cmd.host.HostUpdateConfigCmdData}
     *      execCmd:                        {@link org.yx.hoststack.center.jobs.cmd.host.HostExecCmdData}
     *  image:
     *      create:                         {@link org.yx.hoststack.center.jobs.cmd.image.CreateImageCmdData}
     *      delete:                         {@link org.yx.hoststack.center.jobs.cmd.image.DeleteImageCmdData}
     *  volume:
     *      create:                         {@link org.yx.hoststack.center.jobs.cmd.volume.CreateVolumeCmdData}
     *      delete:                         {@link org.yx.hoststack.center.jobs.cmd.volume.DeleteVolumeCmdData}
     *      mount:                          {@link org.yx.hoststack.center.jobs.cmd.volume.MountVolumeCmdData}
     *      unmount:                        {@link org.yx.hoststack.center.jobs.cmd.volume.UnMountVolumeCmdData}
     *      upgrade:                        {@link org.yx.hoststack.center.jobs.cmd.volume.UpgradeVolumeCmdData}
     *  container:
     *      create:                         {@link org.yx.hoststack.center.jobs.cmd.container.CreateContainerCmdData}
     *      upgrade:                        {@link org.yx.hoststack.center.jobs.cmd.container.UpgradeContainerCmdData}
     *      updateProfile:                  {@link org.yx.hoststack.center.jobs.cmd.container.UpdateProfileCmdData}
     *      start/shutdown/reboot/drop:     {@link org.yx.hoststack.center.jobs.cmd.container.CtrlContainerCmdData}
     *      execCmd:                        {@link org.yx.hoststack.center.jobs.cmd.container.ContainerExecCmdData}
     */
    public <T> JobInnerCmd<T> createJob(@NotNull JobCmd<T> jobCmd) {
        JobInnerCmd<T> jobInnerCmd = buildJobInnerCmd(jobCmd, 0);
        String jobId = jobMap.get(jobCmd.getJobType().getName()).safetyCreateJob(jobInnerCmd);
        if (StringUtil.isNotBlank(jobId)) {
            return jobInnerCmd;
        } else {
            return null;
        }
    }

    /**
     * create jobChain
     * @param jobCmdChain jobChain
     *               example: createAndStartContainer
     *                          1: createParentShellJob
     *                          2: createUserVolume;
     *                          3: createBaseVolume;
     *                          4: createContainer;
     *                          5: mountBaseAndUserVolume;
     *                          6: startContainer;
     * @return rootJobId
     */
    public JobInnerCmd<?> createJobs(@NotNull JobCmdChain jobCmdChain) {
        return transactionTemplate.execute(status -> {
            KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                    .p(LogFieldConstants.ACTION, "ComboJob")
                    .p("JobData", JSON.toJSONString(jobCmdChain));

            int runOrder = 0;
            List<JobInnerCmd<?>> innerCmdList = Lists.newArrayList();
            JobCmdChain.Node<JobCmd<?>> headNode = jobCmdChain.getHead();
            innerCmdList.add(buildJobInnerCmd(headNode.getData(), runOrder));
            JobCmdChain.Node<JobCmd<?>> nextNode = headNode.getNext();
            runOrder++;
            while (nextNode != null) {
                innerCmdList.add(buildJobInnerCmd(nextNode.getData(), runOrder));
                nextNode = nextNode.getNext();
                runOrder++;
            }
            String rootJobId = innerCmdList.getFirst().getJobId();
            for (int i = 0; i < innerCmdList.size(); i++) {
                if (i + 1 < innerCmdList.size()) {
                    innerCmdList.get(i).setNextJobId(innerCmdList.get(i + 1).getJobId());
                }
                innerCmdList.get(i).setRootJobId(rootJobId);
            }
            try {
                for (JobInnerCmd<?> innerCmd : innerCmdList) {
                    jobMap.get(innerCmd.getJobType().getName()).createJob(innerCmd);
                }
                JobInnerCmd<?> firstJob = innerCmdList.stream().filter(jobInnerCmd -> jobInnerCmd.getRunOrder() == 1).toList().getFirst();
                kvLogger.p("RootJobId", innerCmdList.getFirst().getJobId())
                        .i();
                return firstJob;
            } catch (Exception ex) {
//                status.setRollbackOnly();
                kvLogger.e(ex);
                throw ex;
            }
        });
    }

    public <T> R<SendJobResult> sendJob(JobInnerCmd<T> jobInnerCmd) {
        IJob job = jobMap.get(jobInnerCmd.getJobType().getName());
        if (job != null) {
            return job.sendJob(jobInnerCmd);
        } else {
            return R.failed(SysCode.x00000700.getValue(), SysCode.x00000700.getMsg());
        }
    }

    public boolean setJobSendResult(R<SendJobResult> sendResult) {
        return transactionTemplate.execute(status -> {
            try {
                if (sendResult.getData().getFail() != null && !sendResult.getData().getFail().isEmpty()) {
                    jobProcessService.updateJobDetail(Wrappers.lambdaUpdate(JobDetail.class)
                            .set(JobDetail::getJobStatus, JobStatusEnum.FAIL.getName())
                            .set(JobDetail::getJobResult, JSON.toJSONString(
                                    R.builder().code(sendResult.getCode()).msg(sendResult.getMsg())
                            ))
                            .set(JobDetail::getJobProgress, 100)
                            .in(JobDetail::getJobDetailId, sendResult.getData().getFail()));

                    int jobProgress = (sendResult.getData().getFail().size() / sendResult.getData().getTotalJobCount()) * 100;
                    if (jobProgress > 100) {
                        jobProgress = 100;
                    }
                    jobProcessService.updateJobInfo(Wrappers.lambdaUpdate(JobInfo.class)
                            .set(JobInfo::getJobStatus,
                                    sendResult.getData().getFail().size() == sendResult.getData().getTotalJobCount() ?
                                            JobStatusEnum.FAIL.getName() : JobStatusEnum.PROCESSING.getName())
                            .set(JobInfo::getJobProgress, jobProgress)
                            .set(JobInfo::getLastUpdateAt, new Timestamp(System.currentTimeMillis()))
                            .eq(JobInfo::getJobId, sendResult.getData().getJobId()));
                } else {
                    jobProcessService.updateJobInfo(Wrappers.lambdaUpdate(JobInfo.class)
                            .set(JobInfo::getJobStatus, JobStatusEnum.PROCESSING.getName())
                            .set(JobInfo::getJobProgress, RandomUtils.insecure().randomInt(1, 20))
                            .set(JobInfo::getLastUpdateAt, new Timestamp(System.currentTimeMillis()))
                            .eq(JobInfo::getJobId, sendResult.getData().getJobId()));
                }
                return true;
            } catch (Exception ex) {
                status.setRollbackOnly();
                return false;
            }
        });
    }

    public void processJobResult(@NotNull JobReportMessage reportMessage) {
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, CenterEvent.PROCESS_JOB_NOTIFY)
                .p(LogFieldConstants.ACTION, CenterEvent.Action.JOB_NOTIFY)
                .p(LogFieldConstants.TRACE_ID, reportMessage.getTraceId())
                .p(HostStackConstants.JOB_TYPE, reportMessage.getJobType())
                .p(HostStackConstants.JOB_SUB_TYPE, reportMessage.getJobSubType())
                .p(HostStackConstants.JOB_ID, reportMessage.getJobId())
                .p(HostStackConstants.JOB_DETAIL_ID, reportMessage.getJobDetailId())
                .p("JobStatus", reportMessage.getStatus())
                .p("JobCode", reportMessage.getCode())
                .p("JobMsg", reportMessage.getMsg())
                .p("JobProgress", reportMessage.getProgress())
                .p("JobOutput", reportMessage.getOutput());

        IJob job = jobMap.get(reportMessage.getJobType());
        if (job != null) {
            kvLogger.i();
            try {
                JobInfo jobInfo = jobProcessService.getJobWithoutJobCmd(reportMessage.getJobId());
                if (jobInfo == null) {
                    kvLogger.p(LogFieldConstants.ERR_MSG, "Can`t find job in DB")
                            .w();
                    return;
                }
                JobDetail jobDetail = jobProcessService.getJobDetail(reportMessage.getJobId(), reportMessage.getJobDetailId());
                if (jobDetail == null) {
                    kvLogger.p(LogFieldConstants.ERR_MSG, "Can`t find jobDetail in DB")
                            .w();
                    return;
                }
                if (StringUtil.isBlank(reportMessage.getJobType()) || StringUtil.isNotBlank(reportMessage.getJobSubType())) {
                    reportMessage.setJobType(jobInfo.getJobType());
                    reportMessage.setJobSubType(jobInfo.getJobSubType());
                }
                reportMessage.setRegion(jobDetail.getRegion());
                reportMessage.setIdc(jobDetail.getIdc());
                reportMessage.setTid(jobInfo.getTenantId());
                // process job report
                job.processJobReportResult(reportMessage);

                // auto send next job
                if (StringUtil.isNotBlank(jobInfo.getNextJobId())) {
                    // TODO if combo job is create-container and imageType is x86-standard-render, need check host has overlay-vm
                    // TODO if is not has overlay-vm, need to create overlay-vm, and combo job status need to update wait
                    // TODO overlay-vm if create ans start success, when it first to init to host-stack then to resume this combo job
                    JobInfo nextJobInfo = jobProcessService.getJob(jobInfo.getNextJobId());
                    JobInnerCmd<?> nextJobCmd = JSON.parseObject(nextJobInfo.getJobInnerCmd(), JobInnerCmd.class);
                    KvLogger senderLogger = KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, CenterEvent.SEND_JOB)
                            .p(LogFieldConstants.ACTION, CenterEvent.Action.SEND_JOB_NEXT_JOB)
                            .p(HostStackConstants.JOB_TYPE, nextJobCmd.getJobType())
                            .p(HostStackConstants.JOB_SUB_TYPE, nextJobCmd.getJobSubType())
                            .p(HostStackConstants.JOB_ID, nextJobCmd.getJobId());
                    R<SendJobResult> sendR = jobMap.get(nextJobInfo.getJobType()).sendJob(nextJobCmd);
                    if (sendR.getCode() != R.ok().getCode()) {
                        senderLogger.i();
                    } else {
                        // job chain send fail, update chain to fail
                        senderLogger.p(LogFieldConstants.Code, sendR.getCode())
                                .p(LogFieldConstants.ERR_MSG, sendR.getMsg())
                                .w();
                        jobProcessService.updateJobChainFail(jobInfo.getRootJobId(), "{\"msg\": \"" + sendR.getMsg() + "\"}");
                    }
                }
            } catch (Exception ex) {
                kvLogger.p(LogFieldConstants.ERR_MSG, ex.getMessage())
                        .e(ex);
            }
        } else {
            kvLogger.p(LogFieldConstants.ERR_MSG, SysCode.x00000700.getMsg())
                    .w();
        }
    }

    private <T> JobInnerCmd<T> buildJobInnerCmd(JobCmd<T> jobCmd, int runOrder) {
        JobInnerCmd<T> jobInnerCmd = new JobInnerCmd<>();
        jobInnerCmd.setTenantId(jobCmd.getTenantId());
        jobInnerCmd.setJobType(jobCmd.getJobType());
        jobInnerCmd.setJobSubType(jobCmd.getJobSubType());
        jobInnerCmd.setJobData(jobCmd.getJobData());
        jobInnerCmd.setRunOrder(runOrder);
        jobInnerCmd.setJobId(jobIdGenerator.generateJobId());
        return jobInnerCmd;
    }
}
