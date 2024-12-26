package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.VolumeMountRelMapper;
import org.yx.hoststack.center.entity.VolumeMountRel;
import org.yx.hoststack.center.service.VolumeMountRelService;

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
public class VolumeMountRelServiceImpl extends ServiceImpl<VolumeMountRelMapper, VolumeMountRel> implements VolumeMountRelService {

    
    private final VolumeMountRelMapper volumeMountRelMapper;

    @Override
    public Page<VolumeMountRel> findPage(VolumeMountRel params) {
        Page<VolumeMountRel> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<VolumeMountRel> query = Wrappers.lambdaQuery(VolumeMountRel.class);
        return volumeMountRelMapper.selectPage(page, query);
    }

    @Override
    public List<VolumeMountRel> findList(VolumeMountRel params){
        LambdaQueryWrapper<VolumeMountRel> query = Wrappers.lambdaQuery(VolumeMountRel.class);
        return volumeMountRelMapper.selectList(query);
    }

    @Override
    public VolumeMountRel findById(Long id) {
        return volumeMountRelMapper.selectById(id);
    }

    @Override
    public boolean insert(VolumeMountRel volumeMountRel) {
        return save(volumeMountRel);
    }

    @Override
    public boolean update(VolumeMountRel volumeMountRel) {
        return updateById(volumeMountRel);
    }

    @Override
    public int delete(Long id) {
        return volumeMountRelMapper.deleteById(id);
    }

}