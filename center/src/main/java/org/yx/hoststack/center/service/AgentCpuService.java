package org.yx.hoststack.center.service;

import org.yx.hoststack.center.entity.AgentCpu;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
/**
 * @author lyc
 * @since 2024-12-19 19:27:24
 */
public interface AgentCpuService extends IService<AgentCpu> {

    Page<AgentCpu> findPage(AgentCpu params);

    List<AgentCpu> findList(AgentCpu params);

    AgentCpu findById(Long id);

    boolean insert(AgentCpu agentCpu);

    boolean update(AgentCpu agentCpu);

    int delete(Long id);

}