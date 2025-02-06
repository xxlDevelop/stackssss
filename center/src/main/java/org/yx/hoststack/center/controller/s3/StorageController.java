package org.yx.hoststack.center.controller.s3;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yx.hoststack.center.common.constant.RequestMappingBase;
import org.yx.hoststack.center.common.req.storage.GetDownloadUrlReq;
import org.yx.hoststack.center.common.req.storage.GetUploadUrlReq;
import org.yx.hoststack.center.service.s3.S3Service;
import org.yx.lib.utils.util.R;

/**
 * 获取对象存储上传和下载地址
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping(RequestMappingBase.admin + RequestMappingBase.storage)
@RequiredArgsConstructor
public class StorageController {

    private final S3Service s3Service;

    @PostMapping("/getuploadurl")
    public R<?> getUploadUrl(@RequestBody @Valid GetUploadUrlReq getUploadUrlReq) {
        return s3Service.generateUploadUrl(getUploadUrlReq);
    }

    @PostMapping("/getdownloadurl")
    public R<?> getDownloadUrl(@RequestBody @Valid GetDownloadUrlReq getDownloadUrlReq) {
        return s3Service.generateDownloadUrl(getDownloadUrlReq);
    }

}