package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.ImageInfoMapper;
import org.yx.hoststack.center.entity.ImageInfo;
import org.yx.hoststack.center.service.ImageInfoService;

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
public class ImageInfoServiceImpl extends ServiceImpl<ImageInfoMapper, ImageInfo> implements ImageInfoService {

    
    private final ImageInfoMapper imageInfoMapper;

    @Override
    public Page<ImageInfo> findPage(ImageInfo params) {
        Page<ImageInfo> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<ImageInfo> query = Wrappers.lambdaQuery(ImageInfo.class);
        return imageInfoMapper.selectPage(page, query);
    }

    @Override
    public List<ImageInfo> findList(ImageInfo params){
        LambdaQueryWrapper<ImageInfo> query = Wrappers.lambdaQuery(ImageInfo.class);
        return imageInfoMapper.selectList(query);
    }

    @Override
    public ImageInfo findById(Long id) {
        return imageInfoMapper.selectById(id);
    }

    @Override
    public boolean insert(ImageInfo imageInfo) {
        return save(imageInfo);
    }

    @Override
    public boolean update(ImageInfo imageInfo) {
        return updateById(imageInfo);
    }

    @Override
    public int delete(Long id) {
        return imageInfoMapper.deleteById(id);
    }

}