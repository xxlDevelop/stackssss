package org.yx.hoststack.center.common.req.storage;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.yx.hoststack.center.common.req.PageReq;
import org.yx.lib.utils.util.StringUtil;

@EqualsAndHashCode(callSuper = true)
@Data
public class StorageIdcObjectListReq extends PageReq {
    private String region;
    private String idc;
    @NotBlank(message = "bucket cannot be empty")
    private String bucket;

    @AssertTrue(message = "region and idc cannot both be empty")
    public boolean isValidLocationConfig() {
        return !(StringUtil.isBlank(region) && StringUtil.isBlank(idc));
    }
}