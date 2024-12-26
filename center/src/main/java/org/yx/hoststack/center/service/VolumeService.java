package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.Volume;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface VolumeService extends IService<Volume> {

    Page<Volume> findPage(Volume params);

    List<Volume> findList(Volume params);

    Volume findById(Long id);

    boolean insert(Volume volume);

    boolean update(Volume volume);

    int delete(Long id);

}