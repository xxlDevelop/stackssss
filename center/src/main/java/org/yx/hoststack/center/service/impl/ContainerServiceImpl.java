package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.ContainerMapper;
import org.yx.hoststack.center.entity.Container;
import org.yx.hoststack.center.service.ContainerService;

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
public class ContainerServiceImpl extends ServiceImpl<ContainerMapper, Container> implements ContainerService {

    
    private final ContainerMapper containerMapper;

    @Override
    public Page<Container> findPage(Container params) {
        Page<Container> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<Container> query = Wrappers.lambdaQuery(Container.class);
        return containerMapper.selectPage(page, query);
    }

    @Override
    public List<Container> findList(Container params){
        LambdaQueryWrapper<Container> query = Wrappers.lambdaQuery(Container.class);
        return containerMapper.selectList(query);
    }

    @Override
    public Container findById(Long id) {
        return containerMapper.selectById(id);
    }

    @Override
    public boolean insert(Container container) {
        return save(container);
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