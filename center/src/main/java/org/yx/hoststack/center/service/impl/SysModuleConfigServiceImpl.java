package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.SysModuleConfigMapper;
import org.yx.hoststack.center.entity.SysModuleConfig;
import org.yx.hoststack.center.service.SysModuleConfigService;

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
public class SysModuleConfigServiceImpl extends ServiceImpl<SysModuleConfigMapper, SysModuleConfig> implements SysModuleConfigService {

    
    private final SysModuleConfigMapper sysModuleConfigMapper;

    @Override
    public Page<SysModuleConfig> findPage(SysModuleConfig params) {
        Page<SysModuleConfig> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<SysModuleConfig> query = Wrappers.lambdaQuery(SysModuleConfig.class);
        return sysModuleConfigMapper.selectPage(page, query);
    }

    @Override
    public List<SysModuleConfig> findList(SysModuleConfig params){
        LambdaQueryWrapper<SysModuleConfig> query = Wrappers.lambdaQuery(SysModuleConfig.class);
        return sysModuleConfigMapper.selectList(query);
    }

    @Override
    public SysModuleConfig findById(Long id) {
        return sysModuleConfigMapper.selectById(id);
    }

    @Override
    public boolean insert(SysModuleConfig sysModuleConfig) {
        return save(sysModuleConfig);
    }

    @Override
    public boolean update(SysModuleConfig sysModuleConfig) {
        return updateById(sysModuleConfig);
    }

    @Override
    public int delete(Long id) {
        return sysModuleConfigMapper.deleteById(id);
    }

}