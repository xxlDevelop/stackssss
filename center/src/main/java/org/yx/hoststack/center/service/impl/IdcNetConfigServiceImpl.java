package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.enums.SysCode;
import org.yx.hoststack.center.common.req.idc.net.IdcNetConfigListReq;
import org.yx.hoststack.center.common.req.idc.net.IdcNetConfigReq;
import org.yx.hoststack.center.common.resp.idc.net.IdcNetConfigListResp;
import org.yx.hoststack.center.mapper.IdcNetConfigMapper;
import org.yx.hoststack.center.entity.IdcNetConfig;
import org.yx.hoststack.center.service.IdcInfoService;
import org.yx.hoststack.center.service.IdcNetConfigService;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;

import java.util.*;
import java.util.stream.Collectors;


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

    /**
     * Save IDC network configurations
     *
     * @param configReqList Configuration request list
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<?> saveConfig(List<IdcNetConfigReq> configReqList) {
        // Validate network address uniqueness
        R<?> validationResult = validateNetworkAddressUniqueness(configReqList);
        if (!(validationResult.getCode() == R.ok().getCode())) {
            return validationResult;
        }

        try {
            List<IdcNetConfig> configList = configReqList.stream()
                    .map(this::convertToEntity)
                    .collect(Collectors.toList());

            // Batch save or update
            boolean saved = saveOrUpdateBatch(configList);
            return saved ? R.ok() : R.failed(SysCode.x00000400.getValue(), SysCode.x00000400.getMsg());
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.IDC_NET_CONFIG_SAVE_EVENT)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.IDC_NET_CONFIG_SAVE_FAILED)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .e(e);
            return R.failed(SysCode.x00030006.getValue(), SysCode.x00030006.getMsg());
        }
    }

    /**
     * Validate network address uniqueness
     */
    private R<?> validateNetworkAddressUniqueness(List<IdcNetConfigReq> configReqList) {
        // Sets to store IP:Port combinations
        Set<String> localCombinations = new HashSet<>();
        Set<String> mappingCombinations = new HashSet<>();

        // Validate uniqueness within the request list
        for (IdcNetConfigReq req : configReqList) {
            // Split local network address
            String[] localParts = req.getLocalNet().split(":");
            if (localParts.length != 2) {
                return R.failed(SysCode.x00030003.getValue(), SysCode.x00030003.getMsg());
            }

            // Split mapping network address
            String[] mappingParts = req.getMappingNet().split(":");
            if (mappingParts.length != 2) {
                return R.failed(SysCode.x00030004.getValue(), SysCode.x00030004.getMsg());
            }

            // Create combinations for checking
            String localCombination = localParts[0] + ":" + localParts[1];
            String mappingCombination = mappingParts[0] + ":" + mappingParts[1];

            // Check for duplicates in current request list
            if (!localCombinations.add(localCombination)) {
                return R.failed(SysCode.x00030001.getValue(), SysCode.x00030001.getMsg());
            }
            if (!mappingCombinations.add(mappingCombination)) {
                return R.failed(SysCode.x00030002.getValue(), SysCode.x00030002.getMsg());
            }
        }

        // Check for existing combinations in database
        List<IdcNetConfig> existingConfigs = list(new LambdaQueryWrapper<IdcNetConfig>()
                .exists("SELECT 1 FROM t_idc_net_config WHERE " +
                        "CONCAT(local_ip, ':', local_port) IN (" +
                        String.join(",", localCombinations.stream()
                                .map(s -> "'" + s + "'")
                                .collect(Collectors.toList())) + ") " +
                        "OR CONCAT(mapping_ip, ':', mapping_port) IN (" +
                        String.join(",", mappingCombinations.stream()
                                .map(s -> "'" + s + "'")
                                .collect(Collectors.toList())) + ")"
                ));

        if (!existingConfigs.isEmpty()) {
            return R.failed(SysCode.x00030005.getValue(), SysCode.x00030005.getMsg());
        }

        return R.ok();
    }
    /**
     * Convert IP:Port string to IdcNetConfig entity
     */
    private IdcNetConfig convertToEntity(IdcNetConfigReq req) {
        // Split local network address
        String[] localParts = req.getLocalNet().split(":");
        String localIp = localParts[0];
        Integer localPort = Integer.parseInt(localParts[1]);

        // Split mapping network address
        String[] mappingParts = req.getMappingNet().split(":");
        String mappingIp = mappingParts[0];
        Integer mappingPort = Integer.parseInt(mappingParts[1]);

        return IdcNetConfig.builder()
                .localIp(localIp)
                .localPort(localPort)
                .mappingIp(mappingIp)
                .mappingPort(mappingPort)
                .mask(req.getMask())
                .gateway(req.getGateway())
                .dns1(req.getDns1())
                .dns2(req.getDns2())
                .netProtocol(req.getNetProtocol())
                .bandwidthInLimit(req.getBandwidthInLimit())
                .bandwidthOutLimit(req.getBandwidthOutLimit())
                .netIspType(req.getNetIspType())
                .ipType(req.getIpType())
                .mappingName(req.getMappingName())
                .build();
    }

    @Override
    public R<?> list(IdcNetConfigListReq req) {
        try {
            // Query network configurations
            List<IdcNetConfig> configs = list(new LambdaQueryWrapper<IdcNetConfig>()
                    .eq(IdcNetConfig::getIdcId, req.getIdcId()));

            // Convert to response objects
            List<IdcNetConfigListResp> respList = configs.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return R.ok(respList);
        } catch (Exception e) {
            log.error("Failed to query IDC network configurations", e);
            return R.failed(SysCode.x00000400.getValue(), SysCode.x00000400.getMsg());
        }
    }

    /**
     * Convert entity to response object
     */
    private IdcNetConfigListResp convertToResponse(IdcNetConfig config) {
        return IdcNetConfigListResp.builder()
                .localNet(config.getLocalIp() + ":" + config.getLocalPort())
                .mappingNet(config.getMappingIp() + ":" + config.getMappingPort())
                .mask(config.getMask())
                .gateway(config.getGateway())
                .dns1(config.getDns1())
                .dns2(config.getDns2())
                .netProtocol(config.getNetProtocol())
                .bandwidthInLimit(config.getBandwidthInLimit())
                .bandwidthOutLimit(config.getBandwidthOutLimit())
                .netIspType(config.getNetIspType())
                .ipType(config.getIpType())
                .mappingName(config.getMappingName())
                .build();
    }

}