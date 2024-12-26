package org.yx.hoststack.center.service;

import org.yx.hoststack.center.entity.AgentGpu;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
/**
 * @author lyc
 * @since 2024-12-19 19:27:24
 */
public interface AgentGpuService extends IService<AgentGpu> {

    Page<AgentGpu> findPage(AgentGpu params);

    List<AgentGpu> findList(AgentGpu params);

    AgentGpu findById(Long id);

    boolean insert(AgentGpu agentGpu);

    boolean update(AgentGpu agentGpu);

    int delete(Long id);

}