package org.yx.hoststack.center.cache;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.cache.model.ContainerProfileTemplateCacheModel;
import org.yx.hoststack.center.common.constant.CenterCacheKeys;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.redis.util.RedissonUtils;
import org.yx.hoststack.center.entity.ContainerProfileTemplate;
import org.yx.hoststack.center.service.ContainerProfileTemplateService;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContainerCache implements ICenterCache {
    private final ContainerProfileTemplateService containerProfileTemplateService;

    @Override
    public void initCache() {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, CenterEvent.INIT_CACHE)
                .p(LogFieldConstants.ACTION, "LoadContainerInfo")
                .i();
        List<ContainerProfileTemplate> containerProfileTemplateList = containerProfileTemplateService.list();
        for (ContainerProfileTemplate containerProfileTemplate : containerProfileTemplateList) {
            String key = String.format(CenterCacheKeys.containerProfileTemplate, containerProfileTemplate.getContainerType(),
                    containerProfileTemplate.getBizType(), containerProfileTemplate.getOsType(), containerProfileTemplate.getArch());
            RedissonUtils.setStr(key, JSON.toJSONString(containerProfileTemplate));
        }
    }

    public ContainerProfileTemplateCacheModel getContainerTemplate(String containerType, String bizType, String osType, String arch) {
        String key = String.format(CenterCacheKeys.containerProfileTemplate, containerType, bizType, osType, arch);
        Object cacheInfo = RedissonUtils.getStr(key);
        if (cacheInfo != null) {
            return JSON.parseObject(cacheInfo.toString(), ContainerProfileTemplateCacheModel.class);
        }
        return null;
    }
}
