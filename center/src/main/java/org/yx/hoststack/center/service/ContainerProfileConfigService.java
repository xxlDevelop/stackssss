package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.ContainerProfileConfig;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface ContainerProfileConfigService extends IService<ContainerProfileConfig> {

    Page<ContainerProfileConfig> findPage(ContainerProfileConfig params);

    List<ContainerProfileConfig> findList(ContainerProfileConfig params);

    ContainerProfileConfig findById(Long id);

    boolean insert(ContainerProfileConfig containerProfileConfig);

    boolean update(ContainerProfileConfig containerProfileConfig);

    int delete(Long id);

}