package org.yx.hoststack.center.ws.session.service;

import org.springframework.transaction.support.TransactionTemplate;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.HashedWheelTimer;
import org.yx.hoststack.center.cache.ServiceDetailCache;
import org.yx.hoststack.center.cache.model.ServiceDetailCacheModel;
import org.yx.hoststack.center.common.CenterEvent;
import org.yx.hoststack.center.common.constant.CenterCacheKeys;
import org.yx.hoststack.center.common.enums.RegisterNodeEnum;
import org.yx.hoststack.center.common.properties.ApplicationsProperties;
import org.yx.hoststack.center.common.redis.util.RedissonUtils;
import org.yx.hoststack.center.entity.ServiceDetail;
import org.yx.hoststack.center.service.ServiceDetailService;
import org.yx.hoststack.center.ws.session.Session;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.SpringContextHolder;

import java.util.Date;

import static org.yx.hoststack.center.common.enums.SysCode.x00000408;
import static org.yx.hoststack.center.ws.CenterServer.globalServerDetailCacheMap;

public abstract class ServiceSession extends Session {

    public ServiceSession(RegisterNodeEnum sessionType, ChannelHandlerContext context, HashedWheelTimer hashedWheelTimer, long sessionTimeout) {
        super(sessionType, context, hashedWheelTimer, sessionTimeout);
    }

    @Override
    public void attr(String attr, String val) {
        RedissonUtils.setLocalCachedMap(String.format(CenterCacheKeys.sessionAttrInfo, this.sessionId), attr, val);
    }

    @Override
    public String attr(String attr) {
        Object cacheVal = RedissonUtils.getLocalCachedMap(String.format(CenterCacheKeys.sessionAttrInfo, sessionId)).get(attr);
        if (cacheVal != null) {
            return cacheVal.toString();
        }
        return null;
    }

    @Override
    public void removeAttr(String attr) {
        RedissonUtils.getLocalCachedMap(String.format(CenterCacheKeys.sessionAttrInfo, sessionId)).remove(attr);
    }

    public ServiceDetailCacheModel registerServiceDetail(String serviceId) {
        KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.Center_WS_SERVER).p(LogFieldConstants.ACTION, "RegisterServiceDetail")
                .p("ServiceId", serviceId)
                .p("IdcSid", idcId)
                .p("RelaySid", relaySid)
                .p("RelayNodeId", relayId)
                .p("IdcNodeId", idcId)
                .i();
        TransactionTemplate transactionTemplate = SpringContextHolder.getBean(TransactionTemplate.class);
        ServiceDetailService serviceDetailService = SpringContextHolder.getBean(ServiceDetailService.class);
        ServiceDetailCache serviceDetailCache = SpringContextHolder.getBean(ServiceDetailCache.class);

        ServiceDetailCacheModel serviceDetailCacheModel = serviceDetailCache.get(serviceId);
        if (serviceDetailCacheModel == null) {
            ServiceDetail newServiceDetail = transactionTemplate.execute(state -> {
                try {
                    ServiceDetail newDetail = ServiceDetail.builder()
                            .healthy(true)
                            .lastHbAt(new Date(System.currentTimeMillis()))
                            .localIp(serviceIp)
                            .type(String.valueOf(sessionType))
                            .version("1.0")
                            .serviceId(serviceId)
                            .build();
                    serviceDetailService.save(newDetail);
                    newDetail.setEdgeId(zone + "-" + region + "-" + sessionType + "-" + String.format("%0" + SpringContextHolder.getBean(ApplicationsProperties.class).getSequenceNumber() + "d", newDetail.getId()));
                    serviceDetailService.updateById(newDetail);
                    return newDetail;
                } catch (Exception ex) {
                    KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.Center_WS_SERVER).p(LogFieldConstants.ACTION, CenterEvent.Action.SERVICE_REGISTER)
                            .p(LogFieldConstants.ERR_MSG, "saveServiceDetailFailed")
                            .p("IdcSid", idcId)
                            .p("RelaySid", relaySid)
                            .p("RelayNodeId", idcId)
                            .i();
                    state.setRollbackOnly();
                    throw ex;
                }
            });
            serviceDetailCacheModel = serviceDetailCache.add(newServiceDetail);
        }
        return serviceDetailCacheModel;
    }
}
