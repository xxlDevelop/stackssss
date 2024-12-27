package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.common.req.container.ContainerCreateReqDTO;
import org.yx.hoststack.center.common.req.container.ContainerPageReqDTO;
import org.yx.hoststack.center.common.resp.comtainer.ContainerCreateRespVO;
import org.yx.hoststack.center.common.resp.comtainer.ContainerPageDBVO;
import org.yx.hoststack.center.entity.Container;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface ContainerService extends IService<Container> {

    IPage<ContainerPageDBVO> findPage(ContainerPageReqDTO dto);

    List<Container> findList(Container params);

    Container findById(Long id);

    ContainerCreateRespVO insert(ContainerCreateReqDTO dto);

    boolean update(Container container);

    int delete(Long id);

}