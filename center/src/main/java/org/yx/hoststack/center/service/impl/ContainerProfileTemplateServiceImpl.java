package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.ContainerProfileTemplateMapper;
import org.yx.hoststack.center.entity.ContainerProfileTemplate;
import org.yx.hoststack.center.service.ContainerProfileTemplateService;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
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
    public Page<ContainerProfileTemplate> findPage(ContainerProfileTemplate params) {
        Page<ContainerProfileTemplate> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<ContainerProfileTemplate> query = Wrappers.lambdaQuery(ContainerProfileTemplate.class);
        return containerProfileTemplateMapper.selectPage(page, query);
    }

    @Override
    public List<ContainerProfileTemplate> findList(ContainerProfileTemplate params){
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