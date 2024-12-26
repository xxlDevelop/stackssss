package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.TenantResourceGroup;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface TenantResourceGroupService extends IService<TenantResourceGroup> {

    Page<TenantResourceGroup> findPage(TenantResourceGroup params);

    List<TenantResourceGroup> findList(TenantResourceGroup params);

    TenantResourceGroup findById(Long id);

    boolean insert(TenantResourceGroup tenantResourceGroup);

    boolean update(TenantResourceGroup tenantResourceGroup);

    int delete(Long id);

}