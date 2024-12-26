package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.ContainerProxyConfigMapper;
import org.yx.hoststack.center.entity.ContainerProxyConfig;
import org.yx.hoststack.center.service.ContainerProxyConfigService;

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
public class ContainerProxyConfigServiceImpl extends ServiceImpl<ContainerProxyConfigMapper, ContainerProxyConfig> implements ContainerProxyConfigService {

    
    private final ContainerProxyConfigMapper containerProxyConfigMapper;

    @Override
    public Page<ContainerProxyConfig> findPage(ContainerProxyConfig params) {
        Page<ContainerProxyConfig> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<ContainerProxyConfig> query = Wrappers.lambdaQuery(ContainerProxyConfig.class);
        return containerProxyConfigMapper.selectPage(page, query);
    }

    @Override
    public List<ContainerProxyConfig> findList(ContainerProxyConfig params){
        LambdaQueryWrapper<ContainerProxyConfig> query = Wrappers.lambdaQuery(ContainerProxyConfig.class);
        return containerProxyConfigMapper.selectList(query);
    }

    @Override
    public ContainerProxyConfig findById(Long id) {
        return containerProxyConfigMapper.selectById(id);
    }

    @Override
    public boolean insert(ContainerProxyConfig containerProxyConfig) {
        return save(containerProxyConfig);
    }

    @Override
    public boolean update(ContainerProxyConfig containerProxyConfig) {
        return updateById(containerProxyConfig);
    }

    @Override
    public int delete(Long id) {
        return containerProxyConfigMapper.deleteById(id);
    }

}