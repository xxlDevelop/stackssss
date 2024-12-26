package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.SysModuleInstallInfo;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface SysModuleInstallInfoService extends IService<SysModuleInstallInfo> {

    Page<SysModuleInstallInfo> findPage(SysModuleInstallInfo params);

    List<SysModuleInstallInfo> findList(SysModuleInstallInfo params);

    SysModuleInstallInfo findById(Long id);

    boolean insert(SysModuleInstallInfo sysModuleInstallInfo);

    boolean update(SysModuleInstallInfo sysModuleInstallInfo);

    int delete(Long id);

}