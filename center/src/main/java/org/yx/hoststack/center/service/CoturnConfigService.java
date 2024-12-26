package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.common.mybatisplus.tool.support.Query;
import org.yx.hoststack.center.entity.CoturnConfig;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface CoturnConfigService extends IService<CoturnConfig> {

    Page<CoturnConfig> findPage(Query query, CoturnConfig params);

    List<CoturnConfig> findList(CoturnConfig params);

    CoturnConfig findById(Long id);

    boolean insert(CoturnConfig coturnConfig);

    boolean update(CoturnConfig coturnConfig);

    int delete(Long id);

}