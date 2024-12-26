package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.TenantInfoMapper;
import org.yx.hoststack.center.entity.TenantInfo;
import org.yx.hoststack.center.service.TenantInfoService;

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
public class TenantInfoServiceImpl extends ServiceImpl<TenantInfoMapper, TenantInfo> implements TenantInfoService {

    
    private final TenantInfoMapper tenantInfoMapper;

    @Override
    public Page<TenantInfo> findPage(TenantInfo params) {
        Page<TenantInfo> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<TenantInfo> query = Wrappers.lambdaQuery(TenantInfo.class);
        return tenantInfoMapper.selectPage(page, query);
    }

    @Override
    public List<TenantInfo> findList(TenantInfo params){
        LambdaQueryWrapper<TenantInfo> query = Wrappers.lambdaQuery(TenantInfo.class);
        return tenantInfoMapper.selectList(query);
    }

    @Override
    public TenantInfo findById(Long id) {
        return tenantInfoMapper.selectById(id);
    }

    @Override
    public boolean insert(TenantInfo tenantInfo) {
        return save(tenantInfo);
    }

    @Override
    public boolean update(TenantInfo tenantInfo) {
        return updateById(tenantInfo);
    }

    @Override
    public int delete(Long id) {
        return tenantInfoMapper.deleteById(id);
    }

}