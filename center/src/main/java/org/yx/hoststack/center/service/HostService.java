package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.Host;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface HostService extends IService<Host> {

    Page<Host> findPage(Host params);

    List<Host> findList(Host params);

    Host findById(Long id);

    boolean insert(Host host);

    boolean update(Host host);

    int delete(Long id);

}