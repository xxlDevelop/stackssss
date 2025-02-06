package org.yx.hoststack.center.cache;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.cache.model.ServiceDetailCacheModel;
import org.yx.hoststack.center.common.constant.CenterCacheKeys;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.redis.util.RedissonUtils;
import org.yx.hoststack.center.entity.ServiceDetail;
import org.yx.hoststack.center.jobs.AgentServiceGroup;
import org.yx.hoststack.center.service.ServiceDetailService;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ServiceDetailCache implements ICenterCache {

    private static final ConcurrentHashMap<String, ServiceDetailCacheModel> localServerDetailCacheMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, List<ServiceDetailCacheModel>> localServerDetailMappingIdcCacheMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, List<ServiceDetailCacheModel>> localServerDetailMappingRelayCacheMap = new ConcurrentHashMap<>();

    private final ServiceDetailService serviceDetailService;

    @Override
    public void initCache() {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, CenterEvent.INIT_CACHE)
                .p(LogFieldConstants.ACTION, "LoadServiceDetail")
                .i();
        List<ServiceDetail> serviceDetails = serviceDetailService.list();
        for (ServiceDetail serviceDetail : serviceDetails) {
            add(serviceDetail);
        }
    }

    public ServiceDetailCacheModel add(ServiceDetail serviceDetail) {
        ServiceDetailCacheModel cacheModel = new ServiceDetailCacheModel();
        BeanUtils.copyProperties(serviceDetail, cacheModel);

        RedissonUtils.setStr(String.format(CenterCacheKeys.serviceDetailInfo, serviceDetail.getServiceId()), JSON.toJSONString(serviceDetail));

        localServerDetailCacheMap.put(serviceDetail.getServiceId(), cacheModel);

        if (serviceDetail.getType().equalsIgnoreCase("IDC")) {
            localServerDetailMappingIdcCacheMap.compute(serviceDetail.getEdgeId(), (k, v) -> {
                if (v == null) {
                    return Lists.newArrayList(cacheModel);
                } else {
                    v.add(cacheModel);
                    return v;
                }
            });
        } else {
            localServerDetailMappingRelayCacheMap.compute(serviceDetail.getEdgeId(), (k, v) -> {
                if (v == null) {
                    return Lists.newArrayList(cacheModel);
                } else {
                    v.add(cacheModel);
                    return v;
                }
            });
        }
        return cacheModel;
    }

    public ServiceDetailCacheModel get(String serviceId) {
        ServiceDetailCacheModel serviceDetailCacheModel = localServerDetailCacheMap.get(serviceId);
        if (serviceDetailCacheModel != null) {
            return serviceDetailCacheModel;
        }
        Object cacheVal = RedissonUtils.getStr(String.format(CenterCacheKeys.serviceDetailInfo, serviceId));
        if (cacheVal != null) {
            serviceDetailCacheModel = JSON.parseObject(cacheVal.toString(), ServiceDetailCacheModel.class);
            localServerDetailCacheMap.put(serviceId, serviceDetailCacheModel);
        }
        return serviceDetailCacheModel;
    }

    public List<ServiceDetailCacheModel> getServiceDetailsByIdc(String idcId) {
        return localServerDetailMappingIdcCacheMap.get(idcId);
    }

    public List<ServiceDetailCacheModel> getServiceDetailsByRelay(String relayId) {
        return localServerDetailMappingRelayCacheMap.get(relayId);
    }

    public List<ServiceDetailCacheModel> getServiceDetailsByEdge(String edgeId) {
        List<ServiceDetailCacheModel> serviceDetailCacheModels = getServiceDetailsByIdc(edgeId);
        if (serviceDetailCacheModels == null || serviceDetailCacheModels.isEmpty()) {
            serviceDetailCacheModels = getServiceDetailsByRelay(edgeId);
        }
        return serviceDetailCacheModels;
    }

    public void removeLocal(String serviceId) {
        localServerDetailCacheMap.remove(serviceId);
    }
}
