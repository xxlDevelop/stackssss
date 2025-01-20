package org.yx.hoststack.center.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.dynamic.datasource.annotation.Master;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.enums.SysCode;
import org.yx.hoststack.center.common.req.channel.SendChannelReq;
import org.yx.hoststack.center.common.req.idc.IdcCreateReq;
import org.yx.hoststack.center.common.req.idc.IdcListReq;
import org.yx.hoststack.center.common.req.idc.IdcUpdateReq;
import org.yx.hoststack.center.common.req.idc.config.IdcConfigSyncReq;
import org.yx.hoststack.center.common.req.idc.net.IdcNetConfigReq;
import org.yx.hoststack.center.common.resp.PageResp;
import org.yx.hoststack.center.common.resp.idc.CreateIdcInfoResp;
import org.yx.hoststack.center.common.resp.idc.IdcListResp;
import org.yx.hoststack.center.entity.IdcInfo;
import org.yx.hoststack.center.mapper.IdcInfoMapper;
import org.yx.hoststack.center.service.IdcInfoService;
import org.yx.hoststack.center.service.center.CenterService;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.lib.utils.constant.CommonConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IdcInfoServiceImpl extends ServiceImpl<IdcInfoMapper, IdcInfo> implements IdcInfoService {


    private final CenterService centerService;


    @Override
    public R<PageResp<IdcListResp>> list(IdcListReq idcListReq) {
        IPage<IdcInfo> page = new Page<>(idcListReq.getCurrent(), idcListReq.getSize());
        page.orders().add(OrderItem.desc("id"));
        LambdaQueryWrapper<IdcInfo> query = Wrappers.lambdaQuery(IdcInfo.class)
                .eq(IdcInfo::getZone, idcListReq.getZone())
                .eq(IdcInfo::getRegion, idcListReq.getRegion());
        page(page, query);

        PageResp<IdcListResp> resultData = new PageResp<>();
        resultData.setCurrent(idcListReq.getCurrent());
        resultData.setSize(idcListReq.getSize());
        resultData.setRecords(page.getRecords().stream().map(IdcListResp::new).toList());
        resultData.setTotal(page.getTotal());
        resultData.setPages(page.getPages());
        return R.ok(resultData);

    }

    @Master
    @Override
    public CreateIdcInfoResp create(IdcCreateReq idcCreateReq) {
        IdcInfo idcInfo = IdcInfo.builder().build();
        BeanUtil.copyProperties(idcCreateReq, idcInfo);
        save(idcInfo);
        return CreateIdcInfoResp.builder().idcId(idcInfo.getId()).build();
    }

    @Override
    @Master
    public boolean update(IdcUpdateReq idcUpdateReq) {
        LambdaUpdateWrapper<IdcInfo> query = Wrappers.lambdaUpdate(IdcInfo.class)
                .eq(IdcInfo::getIdc, idcUpdateReq.getIdc())
                .set(IdcInfo::getLocalHsIdcHttpSvc, idcUpdateReq.getLocalHsIdcHttpSvc())
                .set(IdcInfo::getNetHsIdcHttpsSvc, idcUpdateReq.getNetHsIdcHttpsSvc())
                .set(IdcInfo::getLocalHsIdcWsSvc, idcUpdateReq.getLocalHsIdcWsSvc())
                .set(IdcInfo::getLocalShareStorageHttpSvc, idcUpdateReq.getLocalShareStorageHttpSvc())
                .set(IdcInfo::getShareStorageUser, idcUpdateReq.getShareStorageUser())
                .set(IdcInfo::getShareStoragePwd, idcUpdateReq.getShareStoragePwd())
                .set(IdcInfo::getLocalLogSvcHttpSvc, idcUpdateReq.getLocalLogSvcHttpSvc())
                .set(IdcInfo::getNetLogSvcHttpsSvc, idcUpdateReq.getNetLogSvcHttpsSvc())
                .set(IdcInfo::getSpeedTestSvc, idcUpdateReq.getSpeedTestSvc())
                .set(IdcInfo::getLocation, idcUpdateReq.getLocation());
        if (!update(query)) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CenterWsServer)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.Update_IdcInfo_Failed)
                    .p("idcInfo", JSON.toJSONString(idcUpdateReq))
                    .i();
            throw new RuntimeException("更新IdcInfo失败！IdcInfo" + JSON.toJSONString(idcUpdateReq));
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<?> syncConfig(List<IdcConfigSyncReq> syncReqList) {
        try {
            if (syncReqList == null || syncReqList.isEmpty()) {
                return R.failed(SysCode.x00000400.getValue(), "Sync request list cannot be empty");
            }

            boolean allSuccess = true;
            for (IdcConfigSyncReq req : syncReqList) {
                if (!syncConfigToIdc(req)) {
                    allSuccess = false;
                    log.error("Failed to sync configuration to IDC: {}", req.getIdcId());
                }
            }

            if (!allSuccess) {
                return R.failed(SysCode.x00030006.getValue(), SysCode.x00030006.getMsg());
            }

            return R.ok();

        } catch (Exception e) {
            log.error("Failed to sync IDC configurations", e);
            return R.failed(SysCode.x00030006.getValue(), "Failed to sync IDC configurations: " + e.getMessage());
        }
    }

    /**
     * 同步配置到指定IDC
     */
    private boolean syncConfigToIdc(IdcConfigSyncReq req) {
        try {
            // Build base configuration
            C2EMessage.EdgeBasicConfig.Builder basicBuilder = C2EMessage.EdgeBasicConfig.newBuilder()
                    .setLocalShareStorageHttpSvc(req.getConfig().getBasic().getLocalShareStorageHttpSvc())
                    .setShareStorageUser(req.getConfig().getBasic().getShareStorageUser())
                    .setShareStoragePwd(req.getConfig().getBasic().getShareStoragePwd())
                    .setLocalLogSvcHttpSvc(req.getConfig().getBasic().getLocalLogSvcHttpSvc())
                    .setNetLogSvcHttpsSvc(req.getConfig().getBasic().getNetLogSvcHttpsSvc())
                    .setSpeedTestSvc(req.getConfig().getBasic().getSpeedTestSvc())
                    .setLocation(req.getConfig().getBasic().getLocation());

            // Build a network configuration list
            List<C2EMessage.EdgeNetConfig> netConfigs = req.getConfig().getNet().stream()
                    .map(IdcNetConfigReq::toEdgeNetConfig)
                    .collect(Collectors.toList());

            // Build a complete configuration synchronization request
            C2EMessage.C2E_EdgeConfigSyncReq configSyncReq = C2EMessage.C2E_EdgeConfigSyncReq.newBuilder()
                    .setBasic(basicBuilder.build())
                    .addAllNet(netConfigs)
                    .build();

            // post IDC Channel
            R<?> sendMsgToLocalOrRemoteChannel = centerService.sendMsgToLocalOrRemoteChannel(
                    SendChannelReq.builder()
                            .serviceId(req.getIdcId())
                            .msg(configSyncReq.toByteArray())
                            .build()
            );
            return sendMsgToLocalOrRemoteChannel.getCode() == R.ok().getCode();

        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.SYNC_CONFIG_TO_IDC_EVENT)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.SYNC_CONFIG_TO_IDC_FAILED)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .p(LogFieldConstants.TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                    .e(e);
            return false;
        }
    }


}