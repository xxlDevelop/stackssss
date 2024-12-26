package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.VolumeMountRel;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface VolumeMountRelService extends IService<VolumeMountRel> {

    Page<VolumeMountRel> findPage(VolumeMountRel params);

    List<VolumeMountRel> findList(VolumeMountRel params);

    VolumeMountRel findById(Long id);

    boolean insert(VolumeMountRel volumeMountRel);

    boolean update(VolumeMountRel volumeMountRel);

    int delete(Long id);

}