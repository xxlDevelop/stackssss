package org.yx.hoststack.center.ws.common.region;

import io.micrometer.common.util.StringUtils;
import org.springframework.util.CollectionUtils;
import org.yx.hoststack.center.entity.RegionInfo;
import org.yx.hoststack.center.ws.CenterServer;

import java.util.List;
import java.util.Optional;

import static org.yx.hoststack.center.ws.CenterServer.globalRegionInfoCacheMap;

/**
 *
 * packageName org.yx.hoststack.center.ws.common.region
 * @author YI-JIAN-ZHANG
 * @version JDK 8
 * @className RegionInfoCache
 * @date 2025/1/15
 */
public class RegionInfoCache {
    /**
     * Get Region By Ip
     *
     * @author yijian
     * @date 2024/12/16 15:50
     */
    public static RegionInfo getRegionByIp(String serviceIp) {
        List<RegionInfo> regionInfos = globalRegionInfoCacheMap.get(CenterServer.REGION_CACHE_KEY);
        if (!CollectionUtils.isEmpty(regionInfos)) {
            Optional<RegionInfo> first = regionInfos.parallelStream().filter(x -> x.getPublicIpList().contains(serviceIp)).findFirst();
            if (first.isPresent()) {
                return first.get();
            }
        }
        return null;
    }


    public static RegionInfo getRegionByZoneRegionCode(String zoneCode,String regionCode) {
        List<RegionInfo> regionInfos = globalRegionInfoCacheMap.get(CenterServer.REGION_CACHE_KEY);
        if (!CollectionUtils.isEmpty(regionInfos)) {
            Optional<RegionInfo> first = regionInfos.parallelStream().filter(x -> {
                boolean matchesZone = true;
                boolean matchesRegion = true;

                if (!StringUtils.isEmpty(zoneCode)) {
                    matchesZone = zoneCode.equals(x.getZoneCode());
                }
                if (!StringUtils.isEmpty(regionCode)) {
                    matchesRegion = regionCode.equals(x.getRegionCode());
                }

                return matchesZone && matchesRegion;
            }).findFirst();
            if (first.isPresent()) {
                return first.get();
            }
        }
        return null;
    }
}
