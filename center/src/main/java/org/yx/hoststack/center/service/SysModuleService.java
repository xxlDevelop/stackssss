package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.SysModule;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface SysModuleService extends IService<SysModule> {

    Page<SysModule> findPage(SysModule params);

    List<SysModule> findList(SysModule params);

    SysModule findById(Long id);

    boolean insert(SysModule sysModule);

    boolean update(SysModule sysModule);

    int delete(Long id);

}