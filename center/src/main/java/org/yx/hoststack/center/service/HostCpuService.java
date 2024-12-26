package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.HostCpu;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface HostCpuService extends IService<HostCpu> {

    Page<HostCpu> findPage(HostCpu params);

    List<HostCpu> findList(HostCpu params);

    HostCpu findById(Long id);

    boolean insert(HostCpu hostCpu);

    boolean update(HostCpu hostCpu);

    int delete(Long id);

}