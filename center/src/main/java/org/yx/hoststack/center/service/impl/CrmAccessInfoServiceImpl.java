package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.CrmAccessInfoMapper;
import org.yx.hoststack.center.entity.CrmAccessInfo;
import org.yx.hoststack.center.service.CrmAccessInfoService;

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
public class CrmAccessInfoServiceImpl extends ServiceImpl<CrmAccessInfoMapper, CrmAccessInfo> implements CrmAccessInfoService {

    
    private final CrmAccessInfoMapper crmAccessInfoMapper;

    @Override
    public Page<CrmAccessInfo> findPage(CrmAccessInfo params) {
        Page<CrmAccessInfo> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<CrmAccessInfo> query = Wrappers.lambdaQuery(CrmAccessInfo.class);
        return crmAccessInfoMapper.selectPage(page, query);
    }

    @Override
    public List<CrmAccessInfo> findList(CrmAccessInfo params){
        LambdaQueryWrapper<CrmAccessInfo> query = Wrappers.lambdaQuery(CrmAccessInfo.class);
        return crmAccessInfoMapper.selectList(query);
    }

    @Override
    public CrmAccessInfo findById(Long id) {
        return crmAccessInfoMapper.selectById(id);
    }

    @Override
    public boolean insert(CrmAccessInfo crmAccessInfo) {
        return save(crmAccessInfo);
    }

    @Override
    public boolean update(CrmAccessInfo crmAccessInfo) {
        return updateById(crmAccessInfo);
    }

    @Override
    public int delete(Long id) {
        return crmAccessInfoMapper.deleteById(id);
    }

}