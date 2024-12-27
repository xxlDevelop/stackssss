package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.common.req.container.ContainerCreateReqDTO;
import org.yx.hoststack.center.common.req.container.ContainerPageReqDTO;
import org.yx.hoststack.center.common.resp.comtainer.ContainerCreateRespVO;
import org.yx.hoststack.center.common.resp.comtainer.ContainerPageDBVO;
import org.yx.hoststack.center.entity.Container;
import org.yx.hoststack.center.mapper.ContainerMapper;
import org.yx.hoststack.center.service.ContainerService;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
public class ContainerServiceImpl extends ServiceImpl<ContainerMapper, Container> implements ContainerService {

    private final ContainerMapper containerMapper;

    public ContainerServiceImpl(ContainerMapper containerMapper) {
        this.containerMapper = containerMapper;
    }

    @Override
    public IPage<ContainerPageDBVO> findPage(ContainerPageReqDTO dto) {

        return containerMapper.getPageList(dto, Page.of(dto == null || dto.getCurrent() == null || dto.getCurrent() <= 0L ? 1L : dto.getCurrent()
                , dto == null || dto.getSize() == null || dto.getSize() <= 0L ? 10L : dto.getSize()));

    }

    @Override
    public List<Container> findList(Container params) {
        LambdaQueryWrapper<Container> query = Wrappers.lambdaQuery(Container.class);
        return baseMapper.selectList(query);
    }

    @Override
    public Container findById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public ContainerCreateRespVO insert(ContainerCreateReqDTO dto) {
//        return save(dto);
        return null;
    }

    @Override
    public boolean update(Container container) {
        return updateById(container);
    }

    @Override
    public int delete(Long id) {
        return baseMapper.deleteById(id);
    }

}