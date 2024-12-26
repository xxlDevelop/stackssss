package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.TenantResourceGroupMapper;
import org.yx.hoststack.center.entity.TenantResourceGroup;
import org.yx.hoststack.center.service.TenantResourceGroupService;

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
public class TenantResourceGroupServiceImpl extends ServiceImpl<TenantResourceGroupMapper, TenantResourceGroup> implements TenantResourceGroupService {

    
    private final TenantResourceGroupMapper tenantResourceGroupMapper;

    @Override
    public Page<TenantResourceGroup> findPage(TenantResourceGroup params) {
        Page<TenantResourceGroup> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<TenantResourceGroup> query = Wrappers.lambdaQuery(TenantResourceGroup.class);
        return tenantResourceGroupMapper.selectPage(page, query);
    }

    @Override
    public List<TenantResourceGroup> findList(TenantResourceGroup params){
        LambdaQueryWrapper<TenantResourceGroup> query = Wrappers.lambdaQuery(TenantResourceGroup.class);
        return tenantResourceGroupMapper.selectList(query);
    }

    @Override
    public TenantResourceGroup findById(Long id) {
        return tenantResourceGroupMapper.selectById(id);
    }

    @Override
    public boolean insert(TenantResourceGroup tenantResourceGroup) {
        return save(tenantResourceGroup);
    }

    @Override
    public boolean update(TenantResourceGroup tenantResourceGroup) {
        return updateById(tenantResourceGroup);
    }

    @Override
    public int delete(Long id) {
        return tenantResourceGroupMapper.deleteById(id);
    }

}