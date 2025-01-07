package org.yx.hoststack.center.common.events.publister;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.yx.hoststack.center.common.events.event.ContainerCreateJobEvent;
import org.yx.hoststack.center.common.events.listener.ContainerCreateJobEventListener;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;


/**
 * @author Lee666
 */
@Slf4j
@Component
public class ContainerCreateJobEventPublisher {

    private final ApplicationEventPublisher publisher;

    public ContainerCreateJobEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(String jobId) {

        ContainerCreateJobEvent customEvent = new ContainerCreateJobEvent(this, jobId);
        publisher.publishEvent(customEvent);
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, ContainerCreateJobEvent.class.getSimpleName())
                .p(LogFieldConstants.ACTION, ContainerCreateJobEventPublisher.class.getSimpleName())
                .p(LogFieldConstants.ReqData, jobId)
                .p(LogFieldConstants.EvtAt, System.currentTimeMillis())
                .p(LogFieldConstants.Success, true)
                .i();
    }
}
