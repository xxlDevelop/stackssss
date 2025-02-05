package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.yx.hoststack.center.common.req.idc.net.IdcNetConfigReq;
import org.yx.hoststack.center.entity.IdcInfo;
import org.yx.hoststack.center.mapper.IdcNetConfigMapper;
import org.yx.hoststack.center.entity.IdcNetConfig;
import org.yx.hoststack.center.service.IdcInfoService;
import org.yx.hoststack.center.service.IdcNetConfigService;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Optional;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IdcNetConfigServiceImpl extends ServiceImpl<IdcNetConfigMapper, IdcNetConfig> implements IdcNetConfigService {

    
    private final IdcNetConfigMapper idcNetConfigMapper;
    private final IdcInfoService idcInfoService;

    @Override
    public Page<IdcNetConfig> findPage(IdcNetConfig params) {
        Page<IdcNetConfig> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<IdcNetConfig> query = Wrappers.lambdaQuery(IdcNetConfig.class);
        return idcNetConfigMapper.selectPage(page, query);
    }

    @Override
    public List<IdcNetConfig> findList(IdcNetConfig params){
        LambdaQueryWrapper<IdcNetConfig> query = Wrappers.lambdaQuery(IdcNetConfig.class);
        return idcNetConfigMapper.selectList(query);
    }

    @Override
    public IdcNetConfig findById(Long id) {
        return idcNetConfigMapper.selectById(id);
    }

    @Override
    public boolean insert(IdcNetConfig idcNetConfig) {
        return save(idcNetConfig);
    }

    @Override
    public boolean update(IdcNetConfig idcNetConfig) {
        return updateById(idcNetConfig);
    }

    @Override
    public int delete(Long id) {
        return idcNetConfigMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveConfig(List<IdcNetConfigReq> idcNetConfigList) {
        for (IdcNetConfigReq idcNetConfigReq : idcNetConfigList){

            Optional<IdcInfo> oneOpt = idcInfoService.getOneOpt(Wrappers.lambdaQuery(IdcInfo.class).eq(IdcInfo::getIdc, idcNetConfigReq.getIdc()));
            /*


            LambdaUpdateWrapper<IdcInfo> query = Wrappers.lambdaUpdate(IdcNetConfigReq.class)
                    .eq(IdcNetConfig::getIdcId, idcNetConfigReq.getIdc())
                    .set(IdcInfo::getLocalHsIdcHttpSvc, idcNetConfigReq.getLocalHsIdcHttpSvc())
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
            }*/
        }
        return true;
    }

}