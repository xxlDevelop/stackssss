package org.yx.hoststack.center.common.req.idc.config;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yx.hoststack.center.common.req.idc.IdcUpdateReq;
import org.yx.hoststack.center.common.req.idc.net.IdcNetConfigReq;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdcConfig {

    @NotNull(message = "Basic configuration cannot be null")
    private IdcUpdateReq basic;

    @NotNull(message = "Network configuration cannot be null")
    private List<IdcNetConfigReq> net;
}
