package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.AdaptedCloudDevice;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface AdaptedCloudDeviceService extends IService<AdaptedCloudDevice> {

    Page<AdaptedCloudDevice> findPage(AdaptedCloudDevice params);

    List<AdaptedCloudDevice> findList(AdaptedCloudDevice params);

    AdaptedCloudDevice findById(Long id);

    boolean insert(AdaptedCloudDevice adaptedCloudDevice);

    boolean update(AdaptedCloudDevice adaptedCloudDevice);

    int delete(Long id);

}