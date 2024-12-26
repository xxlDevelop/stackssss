package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.HostCpuMapper;
import org.yx.hoststack.center.entity.HostCpu;
import org.yx.hoststack.center.service.HostCpuService;

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
public class HostCpuServiceImpl extends ServiceImpl<HostCpuMapper, HostCpu> implements HostCpuService {

    
    private final HostCpuMapper hostCpuMapper;

    @Override
    public Page<HostCpu> findPage(HostCpu params) {
        Page<HostCpu> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<HostCpu> query = Wrappers.lambdaQuery(HostCpu.class);
        return hostCpuMapper.selectPage(page, query);
    }

    @Override
    public List<HostCpu> findList(HostCpu params){
        LambdaQueryWrapper<HostCpu> query = Wrappers.lambdaQuery(HostCpu.class);
        return hostCpuMapper.selectList(query);
    }

    @Override
    public HostCpu findById(Long id) {
        return hostCpuMapper.selectById(id);
    }

    @Override
    public boolean insert(HostCpu hostCpu) {
        return save(hostCpu);
    }

    @Override
    public boolean update(HostCpu hostCpu) {
        return updateById(hostCpu);
    }

    @Override
    public int delete(Long id) {
        return hostCpuMapper.deleteById(id);
    }

}