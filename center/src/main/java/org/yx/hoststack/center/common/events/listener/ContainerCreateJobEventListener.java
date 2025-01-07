package org.yx.hoststack.center.common.events.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.yx.hoststack.center.common.events.event.ContainerCreateJobEvent;
import org.yx.hoststack.center.entity.JobInfo;
import org.yx.hoststack.center.service.JobDetailService;
import org.yx.hoststack.center.service.JobInfoService;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @Description : User aksk check and init event listener
 * @Author : Lee666
 * @Date : 2024/12/17
 * @Version : 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContainerCreateJobEventListener implements ApplicationListener<ContainerCreateJobEvent> {

    private final JobInfoService jobInfoService;
    private final JobDetailService jobDetailService;


    @Async(value = "asyncTaskExecutor")
    @Override
    public void onApplicationEvent(ContainerCreateJobEvent event) {
        LocalDateTime currentTime = LocalDateTime.now();
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, ContainerCreateJobEvent.class.getSimpleName())
                .p(LogFieldConstants.ACTION, ContainerCreateJobEventListener.class.getSimpleName())
                .p(LogFieldConstants.ReqData, event.getJobId());
        Optional<JobInfo> tenantInfoOptional = jobInfoService.getOptById(event.getJobId());
        if (tenantInfoOptional.isEmpty()) {
            kvLogger
                    .p(LogFieldConstants.EvtAt, currentTime)
                    .p(LogFieldConstants.ERR_MSG, "The JobId query for ContainerCreateJobEvent shows that the job info not found")
                    .w();
            return;
        }

        kvLogger
                .p(LogFieldConstants.Success, true)
                .p(LogFieldConstants.EVENT, currentTime)
                .i();

    }


}
