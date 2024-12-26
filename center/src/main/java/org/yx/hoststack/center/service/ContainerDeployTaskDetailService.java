package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.ContainerDeployTaskDetail;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface ContainerDeployTaskDetailService extends IService<ContainerDeployTaskDetail> {

    Page<ContainerDeployTaskDetail> findPage(ContainerDeployTaskDetail params);

    List<ContainerDeployTaskDetail> findList(ContainerDeployTaskDetail params);

    ContainerDeployTaskDetail findById(Long id);

    boolean insert(ContainerDeployTaskDetail containerDeployTaskDetail);

    boolean update(ContainerDeployTaskDetail containerDeployTaskDetail);

    int delete(Long id);

}