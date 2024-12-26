package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.CrmAccessInfo;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface CrmAccessInfoService extends IService<CrmAccessInfo> {

    Page<CrmAccessInfo> findPage(CrmAccessInfo params);

    List<CrmAccessInfo> findList(CrmAccessInfo params);

    CrmAccessInfo findById(Long id);

    boolean insert(CrmAccessInfo crmAccessInfo);

    boolean update(CrmAccessInfo crmAccessInfo);

    int delete(Long id);

}