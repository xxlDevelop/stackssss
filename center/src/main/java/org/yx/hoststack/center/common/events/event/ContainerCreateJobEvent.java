package org.yx.hoststack.center.common.events.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

/**
 * event of Container create job
 * @author Lee666
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class ContainerCreateJobEvent extends ApplicationEvent {
    @Serial
    private static final long serialVersionUID = 4036655943973715546L;

    private final String jobId;

    public ContainerCreateJobEvent(Object source, String jobId) {
        super(source);
        this.jobId = jobId;
    }
}
