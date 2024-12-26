package org.yx.hoststack.center.service;

import org.yx.hoststack.center.entity.RegionInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
/**
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
public interface RegionInfoService extends IService<RegionInfo> {

    Page<RegionInfo> findPage(RegionInfo params);

    List<RegionInfo> findList(RegionInfo params);

    RegionInfo findById(Long id);

    boolean insert(RegionInfo regionInfo);

    boolean update(RegionInfo regionInfo);

    int delete(Long id);

}