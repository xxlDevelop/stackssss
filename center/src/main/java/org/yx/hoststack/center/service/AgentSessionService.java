package org.yx.hoststack.center.service;

import org.yx.hoststack.center.entity.AgentSession;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
/**
 * @author lyc
 * @since 2024-12-19 19:27:24
 */
public interface AgentSessionService extends IService<AgentSession> {

    Page<AgentSession> findPage(AgentSession params);

    List<AgentSession> findList(AgentSession params);

    AgentSession findById(Long id);

    boolean insert(AgentSession agentSession);

    boolean update(AgentSession agentSession);

    int delete(Long id);

}