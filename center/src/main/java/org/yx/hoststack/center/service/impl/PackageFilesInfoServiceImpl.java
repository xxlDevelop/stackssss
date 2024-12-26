package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.PackageFilesInfoMapper;
import org.yx.hoststack.center.entity.PackageFilesInfo;
import org.yx.hoststack.center.service.PackageFilesInfoService;

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
public class PackageFilesInfoServiceImpl extends ServiceImpl<PackageFilesInfoMapper, PackageFilesInfo> implements PackageFilesInfoService {

    
    private final PackageFilesInfoMapper packageFilesInfoMapper;

    @Override
    public Page<PackageFilesInfo> findPage(PackageFilesInfo params) {
        Page<PackageFilesInfo> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<PackageFilesInfo> query = Wrappers.lambdaQuery(PackageFilesInfo.class);
        return packageFilesInfoMapper.selectPage(page, query);
    }

    @Override
    public List<PackageFilesInfo> findList(PackageFilesInfo params){
        LambdaQueryWrapper<PackageFilesInfo> query = Wrappers.lambdaQuery(PackageFilesInfo.class);
        return packageFilesInfoMapper.selectList(query);
    }

    @Override
    public PackageFilesInfo findById(Long id) {
        return packageFilesInfoMapper.selectById(id);
    }

    @Override
    public boolean insert(PackageFilesInfo packageFilesInfo) {
        return save(packageFilesInfo);
    }

    @Override
    public boolean update(PackageFilesInfo packageFilesInfo) {
        return updateById(packageFilesInfo);
    }

    @Override
    public int delete(Long id) {
        return packageFilesInfoMapper.deleteById(id);
    }

}