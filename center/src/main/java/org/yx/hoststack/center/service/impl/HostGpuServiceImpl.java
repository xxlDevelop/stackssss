package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.HostGpuMapper;
import org.yx.hoststack.center.entity.HostGpu;
import org.yx.hoststack.center.service.HostGpuService;

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
public class HostGpuServiceImpl extends ServiceImpl<HostGpuMapper, HostGpu> implements HostGpuService {

    
    private final HostGpuMapper hostGpuMapper;

    @Override
    public Page<HostGpu> findPage(HostGpu params) {
        Page<HostGpu> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<HostGpu> query = Wrappers.lambdaQuery(HostGpu.class);
        return hostGpuMapper.selectPage(page, query);
    }

    @Override
    public List<HostGpu> findList(HostGpu params){
        LambdaQueryWrapper<HostGpu> query = Wrappers.lambdaQuery(HostGpu.class);
        return hostGpuMapper.selectList(query);
    }

    @Override
    public HostGpu findById(Long id) {
        return hostGpuMapper.selectById(id);
    }

    @Override
    public boolean insert(HostGpu hostGpu) {
        return save(hostGpu);
    }

    @Override
    public boolean update(HostGpu hostGpu) {
        return updateById(hostGpu);
    }

    @Override
    public int delete(Long id) {
        return hostGpuMapper.deleteById(id);
    }

}