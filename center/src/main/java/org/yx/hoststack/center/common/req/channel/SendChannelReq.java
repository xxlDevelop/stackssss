package org.yx.hoststack.center.common.req.channel;

import cn.hutool.core.codec.Base64;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendChannelReq {
    private String serviceId;
    private String hostId;
    @NotNull(message = "msg cannot be null")
    private byte[] msg;

    @AssertTrue(message = "Cannot be null at the same time")
    public boolean isValid() {
        return serviceId != null || hostId != null;
    }

    @Override
    public String toString() {
        return "ChannelRequestDTO{" +
                "serviceId='" + serviceId + '\'' +
                ", hostId='" + hostId + '\'' +
                ", msg='" + Base64.encode(msg) + '\'' +
                '}';
    }
}