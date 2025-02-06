package org.yx.hoststack.center.common.constant;

public interface CenterCacheKeys {
    String keyPrefix = "host_stack:center:";

    /**
     * session:attr:sessionId
     */
    String sessionAttrInfo = keyPrefix + "session:attr:%s";

    /**
     * serviceDetail:serviceId
     */
    String serviceDetailInfo = keyPrefix + "serviceDetail:%s";

    /**
     * image:imageId_imageVer
     */
    String imageInfo = keyPrefix + "image:%s_%s";

    /**
     * imageDl:region_idc
     */
    String imageRegionIdcDownloadInfo = keyPrefix + "imageDl:%s_%s";

    /**
     * imageDl:idc
     */
    String imageIdcDownloadInfo = keyPrefix + "imageDl:%s";

    /**
     * imageDl:region_idc_imageId_imageVer
     */
    String imageRegionIdcDownloadInfoByRegionIdc = keyPrefix + "imageDl:%s_%s_%s_%s";

    /**
     * imageDl:idc_imageId_imageVer
     */
    String imageIdcDownloadInfoByIdc = keyPrefix + "imageDl:%s_%s_%s";

    /**
     * container:template:containerType_bizType_osType_arch
     */
    String containerProfileTemplate = keyPrefix + "container:template:%s_%s_%s_%s";

}
