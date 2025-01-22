package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.common.req.image.CreateImageReq;
import org.yx.hoststack.center.common.req.image.ImageListReq;
import org.yx.hoststack.center.common.req.image.ImageStatusReq;
import org.yx.hoststack.center.common.req.image.UpdateImageReq;
import org.yx.hoststack.center.common.resp.PageResp;
import org.yx.hoststack.center.common.resp.image.ImageListResp;
import org.yx.hoststack.center.entity.ImageInfo;
import org.yx.lib.utils.util.R;

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

    /**
     * Creating image information
     *
     * @param req Create image request
     * @return Create response result
     */
    R<?> createImage(CreateImageReq req);

    /**
     * update image information
     *
     * @param req Update image request
     * @return Update response result
     */
    R<?> updateImage(UpdateImageReq req);

    /**
     * Creating image information
     * Call if internal addressing cannot be found
     *
     * @param req Create image request
     * @return Create response result
     */
    R<?> createImageFromRemote(CreateImageReq req);

    /**
     * Get ImageInfo by MD5
     *
     * @param md5 Image MD5 value
     * @return ImageInfo object
     */
    ImageInfo getByMd5(String md5);

    /**
     * Get ImageInfo list
     *
     * @param imageListReq request parameters
     * @return R<PageResp < IdcListResp>> object
     */
    R<PageResp<ImageListResp>> list(ImageListReq imageListReq);

    /**
     * Modified state
     *
     * @param req request parameters
     * @return R<?> object
     */
    R<?> updateStatus(ImageStatusReq req);
}