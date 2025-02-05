package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.Container;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface ContainerService extends IService<Container> {

    Page<Container> findPage(Container params);

    List<Container> findList(Container params);

    Container findById(Long id);

    boolean insert(Container container);

    boolean update(Container container);

    int delete(Long id);

}