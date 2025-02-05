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
import org.yx.hoststack.center.common.req.idc.IdcCreateReq;
import org.yx.hoststack.center.common.req.idc.IdcListReq;
import org.yx.hoststack.center.common.req.idc.IdcUpdateReq;
import org.yx.hoststack.center.common.resp.idc.CreateIdcInfoResp;
import org.yx.hoststack.center.common.resp.idc.IdcListResp;
import org.yx.hoststack.center.entity.IdcInfo;
import org.yx.hoststack.center.mapper.IdcInfoMapper;
import org.yx.hoststack.center.service.IdcInfoService;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.ArrayList;
import java.util.List;

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

}