package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.SysModuleMapper;
import org.yx.hoststack.center.entity.SysModule;
import org.yx.hoststack.center.service.SysModuleService;

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
public class SysModuleServiceImpl extends ServiceImpl<SysModuleMapper, SysModule> implements SysModuleService {

    
    private final SysModuleMapper sysModuleMapper;

    @Override
    public Page<SysModule> findPage(SysModule params) {
        Page<SysModule> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<SysModule> query = Wrappers.lambdaQuery(SysModule.class);
        return sysModuleMapper.selectPage(page, query);
    }

    @Override
    public List<SysModule> findList(SysModule params){
        LambdaQueryWrapper<SysModule> query = Wrappers.lambdaQuery(SysModule.class);
        return sysModuleMapper.selectList(query);
    }

    @Override
    public SysModule findById(Long id) {
        return sysModuleMapper.selectById(id);
    }

    @Override
    public boolean insert(SysModule sysModule) {
        return save(sysModule);
    }

    @Override
    public boolean update(SysModule sysModule) {
        return updateById(sysModule);
    }

    @Override
    public int delete(Long id) {
        return sysModuleMapper.deleteById(id);
    }

}