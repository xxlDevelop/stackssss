package org.yx.hoststack.center.common.req.idc.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yx.hoststack.center.common.req.idc.IdcUpdateReq;
import org.yx.hoststack.center.common.req.idc.net.IdcNetConfigReq;

import java.util.List;

/**
 * IDC Configuration Sync Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdcConfigSyncReq {

    @NotBlank(message = "Region cannot be empty")
    private String region;

    @NotBlank(message = "IDC identifier cannot be empty")
    private String idcId;

    @NotNull(message = "Configuration cannot be null")
    @Valid
    private IdcConfig config;
}

