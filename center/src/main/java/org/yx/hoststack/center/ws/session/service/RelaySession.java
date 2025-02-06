package org.yx.hoststack.center.ws.session.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.HashedWheelTimer;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ObjectUtils;
import org.yx.hoststack.center.cache.model.ServiceDetailCacheModel;
import org.yx.hoststack.center.common.CenterEvent;
import org.yx.hoststack.center.common.enums.RegisterNodeEnum;
import org.yx.hoststack.center.entity.RegionInfo;
import org.yx.hoststack.center.entity.RelayInfo;
import org.yx.hoststack.center.service.RelayInfoService;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.E2CMessage;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.SpringContextHolder;

import static org.yx.hoststack.center.common.enums.SysCode.x00000407;
import static org.yx.hoststack.center.common.enums.SysCode.x00000408;
import static org.yx.hoststack.center.ws.CenterServer.globalRelayInfoCacheMap;
import static org.yx.hoststack.center.ws.common.region.RegionInfoCache.getRegionByIp;

/**
 *
 * packageName org.yx.hoststack.center.ws.session
 * @author YI-JIAN-ZHANG
 * @version JDK 8
 * @className RelaySession
 * @date 2025/1/21
 */
public class RelaySession extends ServiceSession {
    public RelaySession(RegisterNodeEnum sessionType, ChannelHandlerContext context, HashedWheelTimer hashedWheelTimer, long sessionTimeout) {
        super(sessionType, context, hashedWheelTimer, sessionTimeout);
    }

    @Override
    public R<?> initialize0(CommonMessageWrapper.CommonMessage message) throws Exception {
        this.sessionId = message.getHeader().getRelaySid();
        E2CMessage.E2C_EdgeRegisterReq edgeRegister = E2CMessage.E2C_EdgeRegisterReq.parseFrom(message.getBody().getPayload());
        String serviceIp = edgeRegister.getServiceIp();
        KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.Center_WS_SERVER).p(LogFieldConstants.ACTION, "InitializeRelayInfo")
                .p("IdcSid", idcId)
                .p("RelaySid", relaySid)
                .p("IdcSid", idcSid)
                .p("ServiceIp", serviceIp)
                .i();
        RegionInfo regionInfo = getRegionByIp(serviceIp);
        if (ObjectUtils.isEmpty(regionInfo)) {
            KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.Center_WS_SERVER).p(LogFieldConstants.ACTION, CenterEvent.Action.INVALID_IP)
                    .p(LogFieldConstants.ERR_MSG, x00000407.getMsg())
                    .p("IdcSid", idcId)
                    .p("RelaySid", relaySid)
                    .p("RelayNodeId", relayId)
                    .i();
            return R.failed(x00000407.getValue(), x00000407.getMsg());
        }

        this.serviceIp = serviceIp;
        this.idcSid = message.getHeader().getIdcSid();
        this.relaySid = message.getHeader().getRelaySid();
        this.tenantId = message.getHeader().getTenantId();
        this.zone = regionInfo.getZoneCode();
        this.region = regionInfo.getRegionCode();
        this.location = regionInfo.getLocation();

        ServiceDetailCacheModel serviceDetail = registerServiceDetail(sessionId);
        if (serviceDetail == null) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.Center_WS_SERVER)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.SERVICE_REGISTER)
                    .p(LogFieldConstants.ERR_MSG, x00000408.getMsg())
                    .p("IdcSid", idcId)
                    .p("RelaySid", relaySid)
                    .p("RelayNodeId", idcId)
                    .i();
            return R.failed(x00000408.getValue(), x00000408.getMsg());
        }
        this.nodeId = serviceDetail.getEdgeId();

        RelayInfoService relayInfoService = SpringContextHolder.getBean(RelayInfoService.class);
        TransactionTemplate transactionTemplate = SpringContextHolder.getBean(TransactionTemplate.class);
        RelayInfo relayInfo = globalRelayInfoCacheMap.get(nodeId);
        if (relayInfo == null) {
            transactionTemplate.execute(state -> {
                try {
                    RelayInfo build = RelayInfo.builder()
                            .zone(zone)
                            .region(region)
                            .relayIp(serviceIp)
                            .location(location)
                            .relay(nodeId)
                            .build();
                    relayInfoService.save(build);
                    globalRelayInfoCacheMap.put(relayId, build);
                    return build;
                } catch (Exception e) {
                    KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, CenterEvent.Center_WS_SERVER)
                            .p(LogFieldConstants.ACTION, CenterEvent.Action.SERVICE_REGISTER)
                            .p(LogFieldConstants.ERR_MSG, "saveRelayInfoFailed")
                            .p("IdcSid", idcId)
                            .p("RelaySid", relaySid)
                            .p("RelayNodeId", idcId)
                            .p("IdcId", idcId)
                            .i();
                    throw e;
                }
            });
        }
        this.context.channel().attr(AttributeKey.valueOf("innerServiceId")).set(sessionId);
        return R.ok();
    }

    @Override
    public void destroy0() {
// TODO update db health status;
    }

    @Override
    public Object getSource() {
        return this;
    }
}
