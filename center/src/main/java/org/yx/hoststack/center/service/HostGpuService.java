package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.HostGpu;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface HostGpuService extends IService<HostGpu> {

    Page<HostGpu> findPage(HostGpu params);

    List<HostGpu> findList(HostGpu params);

    HostGpu findById(Long id);

    boolean insert(HostGpu hostGpu);

    boolean update(HostGpu hostGpu);

    int delete(Long id);

}