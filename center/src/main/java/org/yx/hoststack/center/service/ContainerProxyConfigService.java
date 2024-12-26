package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.ContainerProxyConfig;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface ContainerProxyConfigService extends IService<ContainerProxyConfig> {

    Page<ContainerProxyConfig> findPage(ContainerProxyConfig params);

    List<ContainerProxyConfig> findList(ContainerProxyConfig params);

    ContainerProxyConfig findById(Long id);

    boolean insert(ContainerProxyConfig containerProxyConfig);

    boolean update(ContainerProxyConfig containerProxyConfig);

    int delete(Long id);

}