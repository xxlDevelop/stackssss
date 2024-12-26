package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.TenantResourceGroupRelMapper;
import org.yx.hoststack.center.entity.TenantResourceGroupRel;
import org.yx.hoststack.center.service.TenantResourceGroupRelService;

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
public class TenantResourceGroupRelServiceImpl extends ServiceImpl<TenantResourceGroupRelMapper, TenantResourceGroupRel> implements TenantResourceGroupRelService {

    
    private final TenantResourceGroupRelMapper tenantResourceGroupRelMapper;

    @Override
    public Page<TenantResourceGroupRel> findPage(TenantResourceGroupRel params) {
        Page<TenantResourceGroupRel> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<TenantResourceGroupRel> query = Wrappers.lambdaQuery(TenantResourceGroupRel.class);
        return tenantResourceGroupRelMapper.selectPage(page, query);
    }

    @Override
    public List<TenantResourceGroupRel> findList(TenantResourceGroupRel params){
        LambdaQueryWrapper<TenantResourceGroupRel> query = Wrappers.lambdaQuery(TenantResourceGroupRel.class);
        return tenantResourceGroupRelMapper.selectList(query);
    }

    @Override
    public TenantResourceGroupRel findById(Long id) {
        return tenantResourceGroupRelMapper.selectById(id);
    }

    @Override
    public boolean insert(TenantResourceGroupRel tenantResourceGroupRel) {
        return save(tenantResourceGroupRel);
    }

    @Override
    public boolean update(TenantResourceGroupRel tenantResourceGroupRel) {
        return updateById(tenantResourceGroupRel);
    }

    @Override
    public int delete(Long id) {
        return tenantResourceGroupRelMapper.deleteById(id);
    }

}