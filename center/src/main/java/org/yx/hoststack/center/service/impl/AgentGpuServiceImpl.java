package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.AgentGpuMapper;
import org.yx.hoststack.center.entity.AgentGpu;
import org.yx.hoststack.center.service.AgentGpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

/**
 * @author lyc
 * @since 2024-12-19 19:27:24
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AgentGpuServiceImpl extends ServiceImpl<AgentGpuMapper, AgentGpu> implements AgentGpuService {

    private AgentGpuMapper agentGpuMapper;

    @Override
    public Page<AgentGpu> findPage(AgentGpu params) {
        Page<AgentGpu> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<AgentGpu> query = Wrappers.lambdaQuery(AgentGpu.class);
        return agentGpuMapper.selectPage(page, query);
    }

    @Override
    public List<AgentGpu> findList(AgentGpu params){
        LambdaQueryWrapper<AgentGpu> query = Wrappers.lambdaQuery(AgentGpu.class);
        return agentGpuMapper.selectList(query);
    }

    @Override
    public AgentGpu findById(Long id) {
        return agentGpuMapper.selectById(id);
    }

    @Override
    public boolean insert(AgentGpu agentGpu) {
        return save(agentGpu);
    }

    @Override
    public boolean update(AgentGpu agentGpu) {
        return updateById(agentGpu);
    }

    @Override
    public int delete(Long id) {
        return agentGpuMapper.deleteById(id);
    }

}