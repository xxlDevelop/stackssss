package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.ImageInfo;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface ImageInfoService extends IService<ImageInfo> {

    Page<ImageInfo> findPage(ImageInfo params);

    List<ImageInfo> findList(ImageInfo params);

    ImageInfo findById(Long id);

    boolean insert(ImageInfo imageInfo);

    boolean update(ImageInfo imageInfo);

    int delete(Long id);

}