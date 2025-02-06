package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yx.hoststack.center.common.req.container.ContainerProfileTemplatePageReqDTO;
import org.yx.hoststack.center.entity.ContainerProfileTemplate;
import org.yx.hoststack.center.mapper.ContainerProfileTemplateMapper;
import org.yx.hoststack.center.service.ContainerProfileTemplateService;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ContainerProfileTemplateServiceImpl extends ServiceImpl<ContainerProfileTemplateMapper, ContainerProfileTemplate> implements ContainerProfileTemplateService {


    private final ContainerProfileTemplateMapper containerProfileTemplateMapper;

    @Override
    public Page<ContainerProfileTemplate> findPage(ContainerProfileTemplatePageReqDTO params) {
        return page(Page.of(params.getCurrent() == null || params.getCurrent() <= 0L ? 1L : params.getCurrent()
                        , params.getSize() == null || params.getSize() <= 0L ? 10L : params.getSize())
                , Wrappers.<ContainerProfileTemplate>lambdaQuery()
                        .select(ContainerProfileTemplate::getBizType, ContainerProfileTemplate::getOsType, ContainerProfileTemplate::getContainerType, ContainerProfileTemplate::getArch, ContainerProfileTemplate::getProfile)
                        .eq(StringUtils.hasLength(params.getBizType()), ContainerProfileTemplate::getBizType, params.getBizType())
                        .eq(StringUtils.hasLength(params.getOsType()), ContainerProfileTemplate::getOsType, params.getOsType())
                        .eq(StringUtils.hasLength(params.getContainerType()), ContainerProfileTemplate::getContainerType, params.getContainerType())
                        .eq(StringUtils.hasLength(params.getArch()), ContainerProfileTemplate::getArch, params.getArch())
        );
    }

    @Override
    public List<ContainerProfileTemplate> findList(ContainerProfileTemplate params) {
        LambdaQueryWrapper<ContainerProfileTemplate> query = Wrappers.lambdaQuery(ContainerProfileTemplate.class);
        return containerProfileTemplateMapper.selectList(query);
    }

    @Override
    public ContainerProfileTemplate findById(Long id) {
        return containerProfileTemplateMapper.selectById(id);
    }

    @Override
    public boolean insert(ContainerProfileTemplate containerProfileTemplate) {
        return save(containerProfileTemplate);
    }

    @Override
    public boolean update(ContainerProfileTemplate containerProfileTemplate) {
        return updateById(containerProfileTemplate);
    }

    @Override
    public int delete(Long id) {
        return containerProfileTemplateMapper.deleteById(id);
    }

}