package org.yx.hoststack.center.common.req.storage;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.yx.lib.utils.util.StringUtil;

@Data
public class CreateBucketReq {
    private String region;
    private String idc;
    @NotBlank(message = "bucket name is required")
    @Pattern(
            regexp = "^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]$",
            message = "Bucket name must be 3-63 characters long and can only contain lowercase letters, numbers, and hyphens"
    )
    private String bucket;//储桶名称符合云存储服务的命名规范  有效的例子：my-bucket test-123 app-bucket-2025 无效的例子：My-Bucket(包含大写字母) -my-bucket(以连字符开头) bucket-(以连字符结尾) a(太短，少于3个字符)

    @AssertTrue(message = "region and idc cannot both be empty")
    public boolean isValidLocationConfig() {
        return !(StringUtil.isBlank(region) && StringUtil.isBlank(idc));
    }
}