package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.common.req.volume.*;
import org.yx.hoststack.center.common.resp.PageResp;
import org.yx.hoststack.center.common.resp.volume.VolumeListResp;
import org.yx.hoststack.center.common.resp.volume.VolumeMountRelResp;
import org.yx.hoststack.center.entity.Volume;
import org.yx.lib.utils.util.R;

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

    /**
     * Creating a data Volume
     *
     * @param req Create data volume request parameters
     * @return Create result
     */
    R<?> createVolume(CreateVolumeReq req);

    R<?> deleteVolumes(DeleteVolumeReq request);

    R<?> mountVolume(MountVolumeReq request);

    R<?> unmountVolume(UnmountVolumeReq request);

    /**
     * Query volume list with pagination
     *
     * @param req Query parameters
     * @return Paginated volume list
     */
    R<PageResp<VolumeListResp>> listVolumes(VolumeListReq req);

    /**
     * Query volume mount relations
     *
     * @param req Query parameters
     * @return Paginated result
     */
    R<PageResp<VolumeMountRelResp>> listVolumeMountRel(VolumeMountRelReq req);


}