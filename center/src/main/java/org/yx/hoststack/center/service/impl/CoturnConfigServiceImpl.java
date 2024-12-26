package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.common.mybatisplus.tool.support.Query;
import org.yx.hoststack.center.mapper.CoturnConfigMapper;
import org.yx.hoststack.center.entity.CoturnConfig;
import org.yx.hoststack.center.service.CoturnConfigService;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CoturnConfigServiceImpl extends ServiceImpl<CoturnConfigMapper, CoturnConfig> implements CoturnConfigService {

    
    private final CoturnConfigMapper coturnConfigMapper;

    @Override
    public Page<CoturnConfig> findPage(Query query, CoturnConfig params) {
        Page<CoturnConfig> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<CoturnConfig> wrapper = Wrappers.lambdaQuery(CoturnConfig.class);
        return coturnConfigMapper.selectPage(page, wrapper);
    }

    @Override
    public List<CoturnConfig> findList(CoturnConfig params){
        LambdaQueryWrapper<CoturnConfig> query = Wrappers.lambdaQuery(CoturnConfig.class);
        return coturnConfigMapper.selectList(query);
    }

    @Override
    public CoturnConfig findById(Long id) {
        return coturnConfigMapper.selectById(id);
    }

    @Override
    public boolean insert(CoturnConfig coturnConfig) {
        return save(coturnConfig);
    }

    @Override
    public boolean update(CoturnConfig coturnConfig) {
        return updateById(coturnConfig);
    }

    @Override
    public int delete(Long id) {
        return coturnConfigMapper.deleteById(id);
    }

}