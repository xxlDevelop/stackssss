package org.yx.hoststack.center.service.s3;

import org.yx.hoststack.center.common.req.storage.GetDownloadUrlReq;
import org.yx.hoststack.center.common.req.storage.GetUploadUrlReq;
import org.yx.lib.utils.util.R;

public interface S3Service {
    /**
     * Generate upload URL
     */
    R<?> generateUploadUrl(GetUploadUrlReq getUploadUrlReq);

    /**
     * Obtain the download address of the object storage device
     *
     * @param getDownloadUrlReq Download request parameters
     * @return Download URL response
     */
    R<?> generateDownloadUrl(GetDownloadUrlReq getDownloadUrlReq);


}