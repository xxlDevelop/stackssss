package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.entity.AdaptedCloudDevice;
import org.yx.hoststack.center.mapper.AdaptedCloudDeviceMapper;
import org.yx.hoststack.center.service.AdaptedCloudDeviceService;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdaptedCloudDeviceServiceImpl extends ServiceImpl<AdaptedCloudDeviceMapper, AdaptedCloudDevice> implements AdaptedCloudDeviceService {


    private final AdaptedCloudDeviceMapper adaptedCloudDeviceMapper;

    @Override
    public Page<AdaptedCloudDevice> findPage(AdaptedCloudDevice params) {
        Page<AdaptedCloudDevice> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<AdaptedCloudDevice> query = Wrappers.lambdaQuery(AdaptedCloudDevice.class);
        return adaptedCloudDeviceMapper.selectPage(page, query);
    }

    @Override
    public List<AdaptedCloudDevice> findList(AdaptedCloudDevice params) {
        LambdaQueryWrapper<AdaptedCloudDevice> query = Wrappers.lambdaQuery(AdaptedCloudDevice.class);
        return adaptedCloudDeviceMapper.selectList(query);
    }

    @Override
    public AdaptedCloudDevice findById(Long id) {
        return adaptedCloudDeviceMapper.selectById(id);
    }

    @Override
    public boolean insert(AdaptedCloudDevice adaptedCloudDevice) {
        return save(adaptedCloudDevice);
    }

    @Override
    public boolean update(AdaptedCloudDevice adaptedCloudDevice) {
        return updateById(adaptedCloudDevice);
    }

    @Override
    public int delete(Long id) {
        return adaptedCloudDeviceMapper.deleteById(id);
    }

}