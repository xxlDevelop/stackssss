package org.yx.hoststack.center.cache;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.cache.model.ImageCacheModel;
import org.yx.hoststack.center.cache.model.ImageDownloadInfCacheModel;
import org.yx.hoststack.center.common.constant.CenterCacheKeys;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.redis.util.RedissonUtils;
import org.yx.hoststack.center.entity.ImageDownloadInf;
import org.yx.hoststack.center.entity.ImageInfo;
import org.yx.hoststack.center.service.ImageDownloadInfService;
import org.yx.hoststack.center.service.ImageInfoService;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.StringPool;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageCache implements ICenterCache {
    private final ImageInfoService imageInfoService;
    private final ImageDownloadInfService imageDownloadInfService;

    @Override
    public void initCache() {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, CenterEvent.INIT_CACHE)
                .p(LogFieldConstants.ACTION, "LoadImageInfo")
                .i();
        List<ImageInfo> imageInfos = imageInfoService.list();
        for (ImageInfo imageInfo : imageInfos) {
            RedissonUtils.setStr(String.format(CenterCacheKeys.imageInfo, imageInfo.getImageId(), imageInfo.getImageVer()), JSON.toJSONString(imageInfo));
        }
        List<ImageDownloadInf> imageDownloadInfs = imageDownloadInfService.list();
        for (ImageDownloadInf imageDownloadInf : imageDownloadInfs) {
            RedissonUtils.setStr(String.format(CenterCacheKeys.imageRegionIdcDownloadInfoByRegionIdc, imageDownloadInf.getRegion(), imageDownloadInf.getIdc(), imageDownloadInf.getImageId(), imageDownloadInf.getImageVer()), JSON.toJSONString(imageDownloadInf));
            RedissonUtils.setStr(String.format(CenterCacheKeys.imageIdcDownloadInfoByIdc, imageDownloadInf.getIdc(), imageDownloadInf.getImageId(), imageDownloadInf.getImageVer()), JSON.toJSONString(imageDownloadInf));
        }
        Map<String, List<ImageDownloadInf>> regionIdcGroupList = imageDownloadInfs.stream().collect(Collectors.groupingBy(item -> item.getRegion() + StringPool.AT + item.getIdc()));
        for (String key : regionIdcGroupList.keySet()) {
            String region = key.split(StringPool.AT)[0];
            String idc = key.split(StringPool.AT)[1];
            RedissonUtils.setStr(String.format(CenterCacheKeys.imageRegionIdcDownloadInfo, region, idc), JSON.toJSONString(regionIdcGroupList.get(key)));
        }
        Map<String, List<ImageDownloadInf>> idcGroupList = imageDownloadInfs.stream().collect(Collectors.groupingBy(ImageDownloadInf::getIdc));
        for (String key : idcGroupList.keySet()) {
            RedissonUtils.setStr(String.format(CenterCacheKeys.imageIdcDownloadInfo, key), JSON.toJSONString(idcGroupList.get(key)));
        }
    }

    public ImageCacheModel getImageInfo(String imageId, String imageVer) {
        Object cacheInfo = RedissonUtils.getStr(String.format(CenterCacheKeys.imageInfo, imageId, imageVer));
        if (cacheInfo != null) {
            return JSON.parseObject(cacheInfo.toString(), ImageCacheModel.class);
        }
        return null;
    }

    public ImageDownloadInfCacheModel getImageDownloadInfo(String idc, String imageId, String imageVer) {
        Object cacheInfo = RedissonUtils.getStr(String.format(CenterCacheKeys.imageIdcDownloadInfoByIdc, idc, imageId, imageVer));
        if (cacheInfo != null) {
            return JSON.parseObject(cacheInfo.toString(), ImageDownloadInfCacheModel.class);
        }
        return null;
    }

    public ImageDownloadInfCacheModel getImageDownloadInfo(String region, String idc, String imageId, String imageVer) {
        Object cacheInfo = RedissonUtils.getStr(String.format(CenterCacheKeys.imageRegionIdcDownloadInfoByRegionIdc, region, idc, imageId, imageVer));
        if (cacheInfo != null) {
            return JSON.parseObject(cacheInfo.toString(), ImageDownloadInfCacheModel.class);
        }
        return null;
    }

    public List<ImageDownloadInfCacheModel> getImageDownloadInfos(String idc) {
        Object cacheInfo = RedissonUtils.getStr(String.format(CenterCacheKeys.imageIdcDownloadInfo, idc));
        if (cacheInfo != null) {
            return JSON.parseArray(cacheInfo.toString(), ImageDownloadInfCacheModel.class);
        }
        return null;
    }

    public List<ImageDownloadInfCacheModel> getImageDownloadInfos(String region, String idc) {
        Object cacheInfo = RedissonUtils.getStr(String.format(CenterCacheKeys.imageRegionIdcDownloadInfo, region, idc));
        if (cacheInfo != null) {
            return JSON.parseArray(cacheInfo.toString(), ImageDownloadInfCacheModel.class);
        }
        return null;
    }

}
