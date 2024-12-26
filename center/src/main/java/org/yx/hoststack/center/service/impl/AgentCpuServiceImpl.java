package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.AgentCpuMapper;
import org.yx.hoststack.center.entity.AgentCpu;
import org.yx.hoststack.center.service.AgentCpuService;
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
public class AgentCpuServiceImpl extends ServiceImpl<AgentCpuMapper, AgentCpu> implements AgentCpuService {

    private AgentCpuMapper agentCpuMapper;

    @Override
    public Page<AgentCpu> findPage(AgentCpu params) {
        Page<AgentCpu> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<AgentCpu> query = Wrappers.lambdaQuery(AgentCpu.class);
        return agentCpuMapper.selectPage(page, query);
    }

    @Override
    public List<AgentCpu> findList(AgentCpu params){
        LambdaQueryWrapper<AgentCpu> query = Wrappers.lambdaQuery(AgentCpu.class);
        return agentCpuMapper.selectList(query);
    }

    @Override
    public AgentCpu findById(Long id) {
        return agentCpuMapper.selectById(id);
    }

    @Override
    public boolean insert(AgentCpu agentCpu) {
        return save(agentCpu);
    }

    @Override
    public boolean update(AgentCpu agentCpu) {
        return updateById(agentCpu);
    }

    @Override
    public int delete(Long id) {
        return agentCpuMapper.deleteById(id);
    }

}