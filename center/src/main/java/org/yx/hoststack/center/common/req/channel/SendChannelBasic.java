package org.yx.hoststack.center.common.req.channel;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class SendChannelBasic {
    public String serviceId;
    public String hostId;
}