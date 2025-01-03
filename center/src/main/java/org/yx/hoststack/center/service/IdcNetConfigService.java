package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.common.req.idc.net.IdcNetConfigReq;
import org.yx.hoststack.center.entity.IdcNetConfig;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface IdcNetConfigService extends IService<IdcNetConfig> {

    Page<IdcNetConfig> findPage(IdcNetConfig params);

    List<IdcNetConfig> findList(IdcNetConfig params);

    IdcNetConfig findById(Long id);

    boolean insert(IdcNetConfig idcNetConfig);

    boolean update(IdcNetConfig idcNetConfig);

    int delete(Long id);

    R<?> saveConfig(List<IdcNetConfigReq> IdcNetConfigReqList);

}