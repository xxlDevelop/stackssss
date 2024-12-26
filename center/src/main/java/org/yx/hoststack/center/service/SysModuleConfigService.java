package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.SysModuleConfig;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface SysModuleConfigService extends IService<SysModuleConfig> {

    Page<SysModuleConfig> findPage(SysModuleConfig params);

    List<SysModuleConfig> findList(SysModuleConfig params);

    SysModuleConfig findById(Long id);

    boolean insert(SysModuleConfig sysModuleConfig);

    boolean update(SysModuleConfig sysModuleConfig);

    int delete(Long id);

}