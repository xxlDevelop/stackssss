package org.yx.hoststack.center.jobs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.dto.ServiceDetailDTO;
import org.yx.hoststack.center.common.enums.JobStatusEnum;
import org.yx.hoststack.center.common.enums.SysCode;
import org.yx.hoststack.center.entity.JobDetail;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.jobs.cmd.module.UpgradeModuleCmdData;
import org.yx.hoststack.center.queue.message.JobReportMessage;
import org.yx.hoststack.center.service.biz.ServerCacheInfoServiceBiz;
import org.yx.hoststack.center.service.center.CenterService;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.protocol.ws.server.JobParams;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.StringPool;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Service("module")
public class ModuleJob extends BaseJob implements IJob {

    @Override
    public String createJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> create(jobCmd, false);
            case UPGRADE -> upgrade(jobCmd, false);
            default -> "";
        };
    }

    @Override
    public String safetyCreateJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> create(jobCmd, true);
            case UPGRADE -> upgrade(jobCmd, true);
            default -> "";
        };
    }

    @Override
    public R<SendJobResult> sendJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> sendCreate(jobCmd);
            case UPGRADE -> sendUpgrade(jobCmd);
            default -> R.failed(SysCode.x00000700.getValue(), SysCode.x00000700.getMsg());
        };
    }

    @Override
    public void processJobReportResult(JobReportMessage reportMessage) {
// TODO
    }

    private String create(JobInnerCmd<?> jobCmd, boolean safety) {
        return null;
    }

    private String upgrade(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        UpgradeModuleCmdData upgradeModuleCmdData = (UpgradeModuleCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("UpgradeModuleData", JSON.toJSONString(upgradeModuleCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String hostId : upgradeModuleCmdData.getHostIds()) {
            ServiceDetailDTO serviceDetail = serverCacheInfoServiceBiz.getAgentInfo(hostId);

            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("hostId", hostId)
                    .fluentPut("moduleName", upgradeModuleCmdData.getModuleName())
                    .fluentPut("version", upgradeModuleCmdData.getVersion())
                    .fluentPut("downloadUrl", upgradeModuleCmdData.getDownloadUrl())
                    .fluentPut("md5", upgradeModuleCmdData.getMd5());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + hostId)
                    .jobHost(hostId)
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(0)
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .idc(serviceDetail.getIdc())
                    .relay(serviceDetail.getRelay())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
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
            ;
            return jobId;
        }
    }

    private R<SendJobResult> sendCreate(JobInnerCmd<?> jobCmd) {
        return R.ok();
    }

    private R<SendJobResult> sendUpgrade(JobInnerCmd<?> jobCmd) {
        String jobId = jobCmd.getJobId();
        UpgradeModuleCmdData upgradeModuleCmdData;
        if (jobCmd.getJobData() instanceof UpgradeModuleCmdData) {
            upgradeModuleCmdData = (UpgradeModuleCmdData) jobCmd.getJobData();
        } else {
            upgradeModuleCmdData = ((JSONObject) jobCmd.getJobData()).toJavaObject(UpgradeModuleCmdData.class);
        }

        // build default result
        SendJobResult sendJobResult = buildDefaultSendResult(jobId, upgradeModuleCmdData.getHostIds().size());
        // group hostId and service rel
        Map<String, List<String>> agentServiceGroupMap = groupAgentIdForService(upgradeModuleCmdData.getHostIds());
        // send job to edge by service
        for (String serviceId : agentServiceGroupMap.keySet()) {
            List<JobParams.ModuleTarget> targetList = Lists.newArrayList();

            for (String hostId : agentServiceGroupMap.get(serviceId)) {
                String jobDetailId = jobId + StringPool.DASH + hostId;

                targetList.add(JobParams.ModuleTarget.newBuilder()
                        .setHostId(hostId)
                        .setJobDetailId(jobDetailId)
                        .build());
            }
            JobParams.ModuleUpgrade jobParams = JobParams.ModuleUpgrade.newBuilder()
                    .setModuleName(upgradeModuleCmdData.getModuleName())
                    .setVersion(upgradeModuleCmdData.getVersion())
                    .setDownloadUrl(upgradeModuleCmdData.getDownloadUrl())
                    .setMd5(upgradeModuleCmdData.getMd5())
                    .addAllTarget(targetList)
                    .build();
            List<String> jobDetailIds = targetList.stream().map(JobParams.ModuleTarget::getJobDetailId).toList();
            R<?> sendR = sendJobToEdge(serviceId, jobCmd, jobParams.toByteString());
            if (sendR.getCode() == R.ok().getCode()) {
                sendJobResult.getSuccess().addAll(jobDetailIds);
            } else {
                sendJobResult.getFail().addAll(jobDetailIds);
            }
        }
        boolean isAllSuccess = sendJobResult.getSuccess().size() == sendJobResult.getTotalJobCount();
        return R.<SendJobResult>builder()
                .code(isAllSuccess ? 0 : SysCode.x00000701.getValue())
                .msg(isAllSuccess ? null : SysCode.x00000701.getMsg())
                .data(sendJobResult)
                .build();
    }
}
