package org.yx.hoststack.center.common.req.idc.net;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yx.hoststack.center.common.req.PageReq;

/**
 * IDC Network Configuration List Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdcNetConfigListReq extends PageReq {

    @NotBlank(message = "IDC identifier cannot be empty")
    private String idcId;
}