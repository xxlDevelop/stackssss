package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.PackageFilesInfo;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface PackageFilesInfoService extends IService<PackageFilesInfo> {

    Page<PackageFilesInfo> findPage(PackageFilesInfo params);

    List<PackageFilesInfo> findList(PackageFilesInfo params);

    PackageFilesInfo findById(Long id);

    boolean insert(PackageFilesInfo packageFilesInfo);

    boolean update(PackageFilesInfo packageFilesInfo);

    int delete(Long id);

}