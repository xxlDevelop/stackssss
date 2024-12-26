package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.ContainerDeployTaskDetailMapper;
import org.yx.hoststack.center.entity.ContainerDeployTaskDetail;
import org.yx.hoststack.center.service.ContainerDeployTaskDetailService;

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
public class ContainerDeployTaskDetailServiceImpl extends ServiceImpl<ContainerDeployTaskDetailMapper, ContainerDeployTaskDetail> implements ContainerDeployTaskDetailService {

    
    private final ContainerDeployTaskDetailMapper containerDeployTaskDetailMapper;

    @Override
    public Page<ContainerDeployTaskDetail> findPage(ContainerDeployTaskDetail params) {
        Page<ContainerDeployTaskDetail> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<ContainerDeployTaskDetail> query = Wrappers.lambdaQuery(ContainerDeployTaskDetail.class);
        return containerDeployTaskDetailMapper.selectPage(page, query);
    }

    @Override
    public List<ContainerDeployTaskDetail> findList(ContainerDeployTaskDetail params){
        LambdaQueryWrapper<ContainerDeployTaskDetail> query = Wrappers.lambdaQuery(ContainerDeployTaskDetail.class);
        return containerDeployTaskDetailMapper.selectList(query);
    }

    @Override
    public ContainerDeployTaskDetail findById(Long id) {
        return containerDeployTaskDetailMapper.selectById(id);
    }

    @Override
    public boolean insert(ContainerDeployTaskDetail containerDeployTaskDetail) {
        return save(containerDeployTaskDetail);
    }

    @Override
    public boolean update(ContainerDeployTaskDetail containerDeployTaskDetail) {
        return updateById(containerDeployTaskDetail);
    }

    @Override
    public int delete(Long id) {
        return containerDeployTaskDetailMapper.deleteById(id);
    }

}