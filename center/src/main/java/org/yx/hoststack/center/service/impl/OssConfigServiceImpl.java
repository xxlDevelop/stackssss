package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.common.dto.OssConfigDetail;
import org.yx.hoststack.center.entity.OssConfig;
import org.yx.hoststack.center.mapper.OssConfigMapper;
import org.yx.hoststack.center.service.OssConfigService;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.List;

import static org.yx.hoststack.center.common.constant.CenterEvent.Action.GET_OSS_CONFIG_BY_REGION_FAIL;
import static org.yx.hoststack.center.common.constant.CenterEvent.OSS_CONFIG_SERVICE_IMPL_EVENT;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OssConfigServiceImpl extends ServiceImpl<OssConfigMapper, OssConfig> implements OssConfigService {


    private final OssConfigMapper ossConfigMapper;
    private final ObjectMapper objectMapper;


    @Override
    public Page<OssConfig> findPage(OssConfig params) {
        Page<OssConfig> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<OssConfig> query = Wrappers.lambdaQuery(OssConfig.class);
        return ossConfigMapper.selectPage(page, query);
    }

    @Override
    public List<OssConfig> findList(OssConfig params) {
        LambdaQueryWrapper<OssConfig> query = Wrappers.lambdaQuery(OssConfig.class);
        return ossConfigMapper.selectList(query);
    }

    @Override
    public OssConfig findById(Long id) {
        return ossConfigMapper.selectById(id);
    }

    @Override
    public boolean insert(OssConfig ossConfig) {
        return save(ossConfig);
    }

    @Override
    public boolean update(OssConfig ossConfig) {
        return updateById(ossConfig);
    }

    @Override
    public int delete(Long id) {
        return ossConfigMapper.deleteById(id);
    }

    public OssConfigDetail getOssConfigByRegion(String region) {
        try {
            OssConfig ossConfig = ossConfigMapper.findByRegion(region);
            if (ossConfig == null) {
                throw new RuntimeException("No OSS configuration found for region: " + region);
            }

            return objectMapper.readValue(ossConfig.getOssConfig(), OssConfigDetail.class);
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, OSS_CONFIG_SERVICE_IMPL_EVENT)
                    .p(LogFieldConstants.ACTION, GET_OSS_CONFIG_BY_REGION_FAIL)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, region)
                    .e(e);
            throw new RuntimeException("Failed to get OSS config: " + e.getMessage());
        }
    }

}