package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.VolumeMapper;
import org.yx.hoststack.center.entity.Volume;
import org.yx.hoststack.center.service.VolumeService;

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
public class VolumeServiceImpl extends ServiceImpl<VolumeMapper, Volume> implements VolumeService {

    
    private final VolumeMapper volumeMapper;

    @Override
    public Page<Volume> findPage(Volume params) {
        Page<Volume> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<Volume> query = Wrappers.lambdaQuery(Volume.class);
        return volumeMapper.selectPage(page, query);
    }

    @Override
    public List<Volume> findList(Volume params){
        LambdaQueryWrapper<Volume> query = Wrappers.lambdaQuery(Volume.class);
        return volumeMapper.selectList(query);
    }

    @Override
    public Volume findById(Long id) {
        return volumeMapper.selectById(id);
    }

    @Override
    public boolean insert(Volume volume) {
        return save(volume);
    }

    @Override
    public boolean update(Volume volume) {
        return updateById(volume);
    }

    @Override
    public int delete(Long id) {
        return volumeMapper.deleteById(id);
    }

}