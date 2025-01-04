package org.yx.hoststack.center.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.Master;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.enums.SysCode;
import org.yx.hoststack.center.common.req.idc.IdcCreateReq;
import org.yx.hoststack.center.common.req.idc.IdcListReq;
import org.yx.hoststack.center.common.req.idc.IdcUpdateReq;
import org.yx.hoststack.center.common.req.idc.config.IdcConfigSyncReq;
import org.yx.hoststack.center.common.resp.idc.CreateIdcInfoResp;
import org.yx.hoststack.center.common.resp.idc.IdcListResp;
import org.yx.hoststack.center.entity.IdcInfo;
import org.yx.hoststack.center.mapper.IdcInfoMapper;
import org.yx.hoststack.center.service.IdcInfoService;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IdcInfoServiceImpl extends ServiceImpl<IdcInfoMapper, IdcInfo> implements IdcInfoService {


    private final IdcInfoMapper idcInfoMapper;


    @Override
    public List<IdcListResp> list(IdcListReq idcListReq) {
        LambdaQueryWrapper<IdcInfo> query = Wrappers.lambdaQuery(IdcInfo.class)
                .eq(IdcInfo::getZone, idcListReq.getZone())
                .eq(IdcInfo::getRegion, idcListReq.getRegion());
        List<IdcInfo> idcInfoList = list(query);
        return idcInfoList.stream().map(IdcListResp::new).toList();

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
        if (!update(query)){
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
            // Validate input
            if (syncReqList == null || syncReqList.isEmpty()) {
                return R.failed(SysCode.x00000400.getValue(), "Sync request list cannot be empty");
            }

            // Process each sync request asynchronously
            List<CompletableFuture<Boolean>> futures = syncReqList.stream()
                    .map(this::processSyncRequest)
                    .collect(Collectors.toList());

            // Wait for all sync operations to complete with timeout
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(IdcConstants.SYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // Check if any sync failed
            boolean allSuccess = futures.stream()
                    .map(CompletableFuture::join)
                    .allMatch(Boolean::booleanValue);

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
     * Process single sync request
     */
    private CompletableFuture<Boolean> processSyncRequest(IdcConfigSyncReq req) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get IDC server address
                String idcServerUrl = getIdcServerUrl(req.getIdcId());
                if (idcServerUrl == null) {
                    log.error("Failed to get IDC server URL for IDC: {}", req.getIdcId());
                    return false;
                }

                // Send configuration to IDC server
                String syncUrl = idcServerUrl + IdcConstants.SYNC_CONFIG_PATH;
                R<?> response = httpUtil.post(syncUrl, req);

                if (!response.isSuccess()) {
                    log.error("Failed to sync configuration to IDC: {}, error: {}",
                            req.getIdcId(), response.getMsg());
                    return false;
                }

                // Update local sync status
                updateSyncStatus(req.getIdcId());

                return true;

            } catch (Exception e) {
                log.error("Error processing sync request for IDC: " + req.getIdcId(), e);
                return false;
            }
        });
    }

    /**
     * Get IDC server URL from IDC info
     */
    private String getIdcServerUrl(String idcId) {
        try {
            IdcInfo idcInfo = idcInfoService.getById(idcId);
            return idcInfo != null ? idcInfo.getLocalHsIdcHttpSvc() : null;
        } catch (Exception e) {
            log.error("Error getting IDC server URL for IDC: " + idcId, e);
            return null;
        }
    }

    /**
     * Update sync status in local database
     */
    private void updateSyncStatus(String idcId) {
        try {
            IdcInfo idcInfo = new IdcInfo();
            idcInfo.setId(idcId);
            idcInfo.setLastSyncTime(new Date());
            idcInfoService.updateById(idcInfo);
        } catch (Exception e) {
            log.error("Error updating sync status for IDC: " + idcId, e);
        }
    }


}