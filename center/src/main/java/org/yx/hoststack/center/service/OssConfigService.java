package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.common.dto.OssConfigDetail;
import org.yx.hoststack.center.entity.OssConfig;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface OssConfigService extends IService<OssConfig> {

    Page<OssConfig> findPage(OssConfig params);

    List<OssConfig> findList(OssConfig params);

    OssConfig findById(Long id);

    boolean insert(OssConfig ossConfig);

    boolean update(OssConfig ossConfig);

    int delete(Long id);

    OssConfigDetail getOssConfigByRegion(String region);

}