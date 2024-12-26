package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.TenantResourceGroupRel;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface TenantResourceGroupRelService extends IService<TenantResourceGroupRel> {

    Page<TenantResourceGroupRel> findPage(TenantResourceGroupRel params);

    List<TenantResourceGroupRel> findList(TenantResourceGroupRel params);

    TenantResourceGroupRel findById(Long id);

    boolean insert(TenantResourceGroupRel tenantResourceGroupRel);

    boolean update(TenantResourceGroupRel tenantResourceGroupRel);

    int delete(Long id);

}