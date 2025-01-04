package org.yx.hoststack.center.common.req.idc.net;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IDC Network Configuration List Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdcNetConfigListReq {

    @NotBlank(message = "IDC identifier cannot be empty")
    private String idcId;
}