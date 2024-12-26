package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.ContainerDeployTask;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface ContainerDeployTaskService extends IService<ContainerDeployTask> {

    Page<ContainerDeployTask> findPage(ContainerDeployTask params);

    List<ContainerDeployTask> findList(ContainerDeployTask params);

    ContainerDeployTask findById(Long id);

    boolean insert(ContainerDeployTask containerDeployTask);

    boolean update(ContainerDeployTask containerDeployTask);

    int delete(Long id);

}