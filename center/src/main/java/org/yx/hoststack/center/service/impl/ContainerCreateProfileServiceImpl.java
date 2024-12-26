package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.entity.ContainerCreateProfile;
import org.yx.hoststack.center.mapper.ContainerCreateProfileMapper;
import org.yx.hoststack.center.service.ContainerCreateProfileService;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ContainerCreateProfileServiceImpl extends ServiceImpl<ContainerCreateProfileMapper, ContainerCreateProfile> implements ContainerCreateProfileService {


    private final ContainerCreateProfileMapper ontainerCreateProfileMapper;

    @Override
    public Page<ContainerCreateProfile> findPage(ContainerCreateProfile params) {
        Page<ContainerCreateProfile> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<ContainerCreateProfile> query = Wrappers.lambdaQuery(ContainerCreateProfile.class);
        return ontainerCreateProfileMapper.selectPage(page, query);
    }

    @Override
    public List<ContainerCreateProfile> findList(ContainerCreateProfile params) {
        LambdaQueryWrapper<ContainerCreateProfile> query = Wrappers.lambdaQuery(ContainerCreateProfile.class);
        return ontainerCreateProfileMapper.selectList(query);
    }

    @Override
    public ContainerCreateProfile findById(Long id) {
        return ontainerCreateProfileMapper.selectById(id);
    }

    @Override
    public boolean insert(ContainerCreateProfile containerCreateProfile) {
        return save(containerCreateProfile);
    }

    @Override
    public boolean update(ContainerCreateProfile containerCreateProfile) {
        return updateById(containerCreateProfile);
    }

    @Override
    public int delete(Long id) {
        return ontainerCreateProfileMapper.deleteById(id);
    }

}