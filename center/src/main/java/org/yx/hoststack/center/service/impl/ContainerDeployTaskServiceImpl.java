package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.ContainerDeployTaskMapper;
import org.yx.hoststack.center.entity.ContainerDeployTask;
import org.yx.hoststack.center.service.ContainerDeployTaskService;

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
public class ContainerDeployTaskServiceImpl extends ServiceImpl<ContainerDeployTaskMapper, ContainerDeployTask> implements ContainerDeployTaskService {

    
    private final ContainerDeployTaskMapper containerDeployTaskMapper;

    @Override
    public Page<ContainerDeployTask> findPage(ContainerDeployTask params) {
        Page<ContainerDeployTask> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<ContainerDeployTask> query = Wrappers.lambdaQuery(ContainerDeployTask.class);
        return containerDeployTaskMapper.selectPage(page, query);
    }

    @Override
    public List<ContainerDeployTask> findList(ContainerDeployTask params){
        LambdaQueryWrapper<ContainerDeployTask> query = Wrappers.lambdaQuery(ContainerDeployTask.class);
        return containerDeployTaskMapper.selectList(query);
    }

    @Override
    public ContainerDeployTask findById(Long id) {
        return containerDeployTaskMapper.selectById(id);
    }

    @Override
    public boolean insert(ContainerDeployTask containerDeployTask) {
        return save(containerDeployTask);
    }

    @Override
    public boolean update(ContainerDeployTask containerDeployTask) {
        return updateById(containerDeployTask);
    }

    @Override
    public int delete(Long id) {
        return containerDeployTaskMapper.deleteById(id);
    }

}