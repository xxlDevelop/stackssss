package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.SysModuleInstallInfoMapper;
import org.yx.hoststack.center.entity.SysModuleInstallInfo;
import org.yx.hoststack.center.service.SysModuleInstallInfoService;

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
public class SysModuleInstallInfoServiceImpl extends ServiceImpl<SysModuleInstallInfoMapper, SysModuleInstallInfo> implements SysModuleInstallInfoService {

    
    private final SysModuleInstallInfoMapper sysModuleInstallInfoMapper;

    @Override
    public Page<SysModuleInstallInfo> findPage(SysModuleInstallInfo params) {
        Page<SysModuleInstallInfo> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<SysModuleInstallInfo> query = Wrappers.lambdaQuery(SysModuleInstallInfo.class);
        return sysModuleInstallInfoMapper.selectPage(page, query);
    }

    @Override
    public List<SysModuleInstallInfo> findList(SysModuleInstallInfo params){
        LambdaQueryWrapper<SysModuleInstallInfo> query = Wrappers.lambdaQuery(SysModuleInstallInfo.class);
        return sysModuleInstallInfoMapper.selectList(query);
    }

    @Override
    public SysModuleInstallInfo findById(Long id) {
        return sysModuleInstallInfoMapper.selectById(id);
    }

    @Override
    public boolean insert(SysModuleInstallInfo sysModuleInstallInfo) {
        return save(sysModuleInstallInfo);
    }

    @Override
    public boolean update(SysModuleInstallInfo sysModuleInstallInfo) {
        return updateById(sysModuleInstallInfo);
    }

    @Override
    public int delete(Long id) {
        return sysModuleInstallInfoMapper.deleteById(id);
    }

}