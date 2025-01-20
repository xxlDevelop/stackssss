package org.yx.hoststack.center.jobs;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.jobs.cmd.JobCmd;
import org.yx.hoststack.center.jobs.cmd.JobCmdChain;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.List;
import java.util.Map;

@Service
public class JobManager {
    private final Map<String, IJob> jobMap;
    private final TransactionTemplate transactionTemplate;
    private final JobIdGenerator jobIdGenerator;

    public JobManager(Map<String, IJob> jobMap, TransactionTemplate transactionTemplate, JobIdGenerator jobIdGenerator) {
        this.jobMap = jobMap;
        this.transactionTemplate = transactionTemplate;
        this.jobIdGenerator = jobIdGenerator;
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
     *      delete:                         {@link List<org.yx.hoststack.center.jobs.cmd.image.DeleteImageCmdData>}
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
    public <T> String createJob(JobCmd<T> jobCmd) {
        JobInnerCmd<T> jobInnerCmd = buildJobInnerCmd(jobCmd, 0);
        return jobMap.get(jobCmd.getJobType().getName()).safetyDoJob(jobInnerCmd);
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
    public String createJobs(@NotNull JobCmdChain jobCmdChain) {
        return transactionTemplate.execute(status -> {
            KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                    .p(LogFieldConstants.ACTION, "comboJob")
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
                    createJobUnSafety(innerCmd);
                }
                kvLogger.p("RootJobId", innerCmdList.getFirst().getJobId())
                        .i();
                return rootJobId;
            } catch (Exception ex) {
                status.setRollbackOnly();
                kvLogger.e(ex);
                return "";
            }
        });
    }

    private <T> String createJobUnSafety(JobInnerCmd<T> jobCmd) {
        return jobMap.get(jobCmd.getJobType().getName()).doJob(jobCmd);
    }

    private <T> JobInnerCmd<T> buildJobInnerCmd(JobCmd<T> jobCmd, int runOrder) {
        JobInnerCmd<T> jobInnerCmd = new JobInnerCmd<>();
        jobInnerCmd.setZone(jobCmd.getZone());
        jobInnerCmd.setRegion(jobCmd.getRegion());
        jobInnerCmd.setIdc(jobCmd.getIdc());
        jobInnerCmd.setRelay(jobCmd.getRelay());
        jobInnerCmd.setTenantId(jobCmd.getTenantId());
        jobInnerCmd.setJobType(jobCmd.getJobType());
        jobInnerCmd.setJobSubType(jobCmd.getJobSubType());
        jobInnerCmd.setJobData(jobCmd.getJobData());
        jobInnerCmd.setRunOrder(runOrder);
        jobInnerCmd.setJobId(jobIdGenerator.generateJobId());
        return jobInnerCmd;
    }
}
