package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.AgentSession;

import java.util.List;

public interface AgentSessionService extends IService<AgentSession> {

    Page<AgentSession> findPage(AgentSession params);

    List<AgentSession> findList(AgentSession params);

    AgentSession findById(Long id);

    boolean insert(AgentSession agentSession);

    boolean update(AgentSession agentSession);

    int delete(Long id);

}
