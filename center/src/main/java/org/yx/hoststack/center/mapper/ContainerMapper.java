package org.yx.hoststack.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.yx.hoststack.center.common.req.container.ContainerPageReqDTO;
import org.yx.hoststack.center.common.resp.comtainer.ContainerPageDBVO;
import org.yx.hoststack.center.entity.Container;

/**
 * 容器信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Mapper
public interface ContainerMapper extends BaseMapper<Container> {

    Page<ContainerPageDBVO> getPageList(@Param("dto") ContainerPageReqDTO dto, IPage<Container> page);
}