package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.AgentSessionMapper;
import org.yx.hoststack.center.entity.AgentSession;
import org.yx.hoststack.center.service.AgentSessionService;
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
public class AgentSessionServiceImpl extends ServiceImpl<AgentSessionMapper, AgentSession> implements AgentSessionService {

    private AgentSessionMapper agentSessionMapper;

    @Override
    public Page<AgentSession> findPage(AgentSession params) {
        Page<AgentSession> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<AgentSession> query = Wrappers.lambdaQuery(AgentSession.class);
        return agentSessionMapper.selectPage(page, query);
    }

    @Override
    public List<AgentSession> findList(AgentSession params){
        LambdaQueryWrapper<AgentSession> query = Wrappers.lambdaQuery(AgentSession.class);
        return agentSessionMapper.selectList(query);
    }

    @Override
    public AgentSession findById(Long id) {
        return agentSessionMapper.selectById(id);
    }

    @Override
    public boolean insert(AgentSession agentSession) {
        return save(agentSession);
    }

    @Override
    public boolean update(AgentSession agentSession) {
        return updateById(agentSession);
    }

    @Override
    public int delete(Long id) {
        return agentSessionMapper.deleteById(id);
    }

}