package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yx.hoststack.center.common.req.container.ContainerCreateReqDTO;
import org.yx.hoststack.center.common.req.container.ContainerPageReqDTO;
import org.yx.hoststack.center.common.resp.comtainer.ContainerCreateRespVO;
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
@RequiredArgsConstructor
public class ContainerServiceImpl extends ServiceImpl<ContainerMapper, Container> implements ContainerService {


    private final ContainerMapper containerMapper;

    @Override
    public Page<Container> findPage(ContainerPageReqDTO params) {
        return page(Page.of(params.getCurrent() == null || params.getCurrent() <= 0L ? 1L : params.getCurrent()
                        , params.getSize() == null || params.getSize() <= 0L ? 10L : params.getSize())
                , Wrappers.<Container>lambdaQuery()
                        .eq(StringUtils.hasLength(params.getZone()), Container::getZone, params.getZone())
                        .eq(StringUtils.hasLength(params.getRegion()), Container::getRegion, params.getRegion())
                        .eq(StringUtils.hasLength(params.getResourcePool()), Container::getResourcePool, params.getResourcePool())
                        .eq(StringUtils.hasLength(params.getOsType()), Container::getOsType, params.getOsType())
        );
    }

    @Override
    public List<Container> findList(Container params) {
        LambdaQueryWrapper<Container> query = Wrappers.lambdaQuery(Container.class);
        return containerMapper.selectList(query);
    }

    @Override
    public Container findById(Long id) {
        return containerMapper.selectById(id);
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
        return containerMapper.deleteById(id);
    }

}