package org.yx.hoststack.center.jobs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.enums.JobStatusEnum;
import org.yx.hoststack.center.entity.JobDetail;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.jobs.cmd.host.HostExecCmdData;
import org.yx.hoststack.center.jobs.cmd.host.HostResetCmdData;
import org.yx.hoststack.center.jobs.cmd.host.HostUpdateConfigCmdData;
import org.yx.hoststack.center.service.JobDetailService;
import org.yx.hoststack.center.service.JobInfoService;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.StringPool;

import java.sql.Timestamp;
import java.util.List;

@Service("host")
public class HostJob extends BaseJob implements IJob {

    public HostJob(JobInfoService jobInfoService, JobDetailService jobDetailService,
                   TransactionTemplate transactionTemplate) {
        super(jobInfoService, jobDetailService, transactionTemplate);
    }

    @Override
    public String doJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case RESET -> reset(jobCmd, false);
            case UPDATE_CONFIG -> updateConfig(jobCmd, false);
            case EXEC_CMD -> execCmd(jobCmd, false);
            default -> "";
        };
    }

    @Override
    public String safetyDoJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case RESET -> reset(jobCmd, true);
            case UPDATE_CONFIG -> updateConfig(jobCmd, true);
            case EXEC_CMD -> execCmd(jobCmd, true);
            default -> "";
        };
    }

    public String reset(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        HostResetCmdData hostResetCmdData = (HostResetCmdData) jobCmd.getJobData();
        List<String> hotIdArray = hostResetCmdData.getHostIds();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("HostIds", String.join(StringPool.COMMA, hotIdArray))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JSONObject> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String hostId : hotIdArray) {
            JSONObject target = new JSONObject()
                    .fluentPut("hostId", hostId)
                    .fluentPut("jobDetailId", jobId + StringPool.DASH + hostId);
            targetList.add(target);

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + hostId)
                    .jobHost(hostId)
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(RandomUtils.insecure().randomInt(1, 50))
                    .jobParams("{\"hostId\": \"" + hostId + "\"}")
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        JSONObject jobParams = new JSONObject()
                .fluentPut("target", targetList);
        return persistence(safety, jobId, jobCmd, jobDetailList, kvLogger);
    }

    public String updateConfig(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        HostUpdateConfigCmdData configCmdData = (HostUpdateConfigCmdData) jobCmd.getJobData();
        List<HostUpdateConfigCmdData.HostConfig> configs = configCmdData.getConfigs();
        List<String> hotIdArray = configCmdData.getHostIds();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("HostIds", String.join(StringPool.COMMA, hotIdArray))
                .p("Configs", JSON.toJSONString(configs))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JSONObject> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String hostId : hotIdArray) {
            JSONObject target = new JSONObject()
                    .fluentPut("hostId", hostId)
                    .fluentPut("jobDetailId", jobId + StringPool.DASH + hostId);
            targetList.add(target);

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + hostId)
                    .jobHost(hostId)
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(RandomUtils.insecure().randomInt(1, 50))
                    .jobParams("{\"hostId\":\"" + hostId + "\",\"config\":\"" + JSON.toJSONString(configs) + "\"}")
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        JSONObject jobParams = new JSONObject()
                .fluentPut("config", configs)
                .fluentPut("target", targetList);
        return persistence(safety, jobId, jobCmd, jobDetailList, kvLogger);
    }

    public String execCmd(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        HostExecCmdData execCmdData = (HostExecCmdData) jobCmd.getJobData();
        List<String> hotIdArray = execCmdData.getHostIds();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("HostIds", String.join(StringPool.COMMA, hotIdArray))
                .p("Script", execCmdData.getScript())
                .p(HostStackConstants.JOB_ID, jobId);

        List<JSONObject> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String hostId : hotIdArray) {
            JSONObject target = new JSONObject()
                    .fluentPut("hostId", hostId)
                    .fluentPut("jobDetailId", jobId + StringPool.DASH + hostId);
            targetList.add(target);

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + hostId)
                    .jobHost(hostId)
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(RandomUtils.insecure().randomInt(1, 50))
                    .jobParams("{\"hostId\":\"" + hostId + "\",\"script\":\"" + execCmdData.getScript() + "\"}")
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        JSONObject jobParams = new JSONObject()
                .fluentPut("script", execCmdData.getScript())
                .fluentPut("target", targetList);

        return persistence(safety, jobId, jobCmd, jobDetailList, kvLogger);
    }

    private String persistence(boolean safety, String jobId, JobInnerCmd<?> jobCmd, List<JobDetail> jobDetailList, KvLogger kvLogger) {
        if (safety) {
            try {
                safetyPersistenceJob(jobId, jobCmd, null, "", jobDetailList);
                kvLogger.i();
                return jobId;
            } catch (Exception ex) {
                kvLogger.p(LogFieldConstants.ERR_MSG, ex.getMessage())
                        .e(ex);
                return "";
            }
        } else {
            persistenceJob(jobId, jobCmd, null, "", jobDetailList);
            kvLogger.i();
            return jobId;
        }
    }
}
