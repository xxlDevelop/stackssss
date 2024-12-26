package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.ContainerProfileConfigMapper;
import org.yx.hoststack.center.entity.ContainerProfileConfig;
import org.yx.hoststack.center.service.ContainerProfileConfigService;

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
public class ContainerProfileConfigServiceImpl extends ServiceImpl<ContainerProfileConfigMapper, ContainerProfileConfig> implements ContainerProfileConfigService {

    
    private final ContainerProfileConfigMapper containerProfileConfigMapper;

    @Override
    public Page<ContainerProfileConfig> findPage(ContainerProfileConfig params) {
        Page<ContainerProfileConfig> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<ContainerProfileConfig> query = Wrappers.lambdaQuery(ContainerProfileConfig.class);
        return containerProfileConfigMapper.selectPage(page, query);
    }

    @Override
    public List<ContainerProfileConfig> findList(ContainerProfileConfig params){
        LambdaQueryWrapper<ContainerProfileConfig> query = Wrappers.lambdaQuery(ContainerProfileConfig.class);
        return containerProfileConfigMapper.selectList(query);
    }

    @Override
    public ContainerProfileConfig findById(Long id) {
        return containerProfileConfigMapper.selectById(id);
    }

    @Override
    public boolean insert(ContainerProfileConfig containerProfileConfig) {
        return save(containerProfileConfig);
    }

    @Override
    public boolean update(ContainerProfileConfig containerProfileConfig) {
        return updateById(containerProfileConfig);
    }

    @Override
    public int delete(Long id) {
        return containerProfileConfigMapper.deleteById(id);
    }

}