package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.TenantInfo;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface TenantInfoService extends IService<TenantInfo> {

    Page<TenantInfo> findPage(TenantInfo params);

    List<TenantInfo> findList(TenantInfo params);

    TenantInfo findById(Long id);

    boolean insert(TenantInfo tenantInfo);

    boolean update(TenantInfo tenantInfo);

    int delete(Long id);

}