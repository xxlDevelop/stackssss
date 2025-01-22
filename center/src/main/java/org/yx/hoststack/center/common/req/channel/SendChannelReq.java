package org.yx.hoststack.center.common.req.channel;

import cn.hutool.core.codec.Base64;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;
import java.util.Objects;

@Data
@SuperBuilder
public class SendChannelReq extends SendChannelBasic {
    @NotNull(message = "msg cannot be null")
    private byte[] msg;

    @AssertTrue(message = "Cannot be null or blank at the same time")
    public boolean isValid() {
        boolean isServiceIdBlank = serviceId == null || serviceId.trim().isEmpty();
        boolean isHostIdBlank = hostId == null || hostId.trim().isEmpty();

        return !(isServiceIdBlank && isHostIdBlank);
    }

    @Override
    public String toString() {
        return "ChannelRequestDTO{" +
                "serviceId='" + serviceId + '\'' +
                ", hostId='" + hostId + '\'' +
                ", msg='" + Base64.encode(msg) + '\'' +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SendChannelReq that = (SendChannelReq) o;
        return Objects.equals(serviceId, that.serviceId) &&
                Objects.equals(hostId, that.hostId) &&
                Arrays.equals(msg, that.msg);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(serviceId, hostId);
        result = 31 * result + Arrays.hashCode(msg);
        return result;
    }
}