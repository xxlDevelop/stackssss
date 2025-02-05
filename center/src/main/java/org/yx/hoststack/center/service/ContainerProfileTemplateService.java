package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.ContainerProfileTemplate;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface ContainerProfileTemplateService extends IService<ContainerProfileTemplate> {

    Page<ContainerProfileTemplate> findPage(ContainerProfileTemplate params);

    List<ContainerProfileTemplate> findList(ContainerProfileTemplate params);

    ContainerProfileTemplate findById(Long id);

    boolean insert(ContainerProfileTemplate containerProfileTemplate);

    boolean update(ContainerProfileTemplate containerProfileTemplate);

    int delete(Long id);

}