package org.yx.hoststack.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.yx.hoststack.center.common.req.volume.VolumeListReq;
import org.yx.hoststack.center.common.req.volume.VolumeMountRelReq;
import org.yx.hoststack.center.common.resp.volume.VolumeListResp;
import org.yx.hoststack.center.common.resp.volume.VolumeMountRelResp;
import org.yx.hoststack.center.entity.Volume;
import org.yx.hoststack.center.jobs.cmd.volume.MountVolumeCmdData;
import org.yx.hoststack.center.jobs.cmd.volume.UnMountVolumeCmdData;

import java.util.List;

/**
 * 存储卷
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Mapper
public interface VolumeMapper extends BaseMapper<Volume> {

    List<MountVolumeCmdData.MountVolumeInfo> selectMountInfoByCidAndVolumeId(
            @Param("cid") String cid,
            @Param("volumeId") String volumeId
    );

    List<UnMountVolumeCmdData.UnMountVolumeInfo> selectUnmountInfoByCidAndVolumeId(
            @Param("cid") String cid,
            @Param("volumeId") String volumeId
    );

    /**
     * Query volumes with mount relations
     */
    IPage<VolumeListResp> selectVolumeList(IPage<VolumeListResp> page,
                                           @Param("req") VolumeListReq req);

    /**
     * Query volume mount relation list with pagination
     *
     * @param page Pagination parameters
     * @param req  Query parameters
     * @return Paginated result
     */
    IPage<VolumeMountRelResp> selectVolumeMountRelList(IPage<?> page,
                                                       @Param("req") VolumeMountRelReq req);

}