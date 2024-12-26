package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.OssConfigMapper;
import org.yx.hoststack.center.entity.OssConfig;
import org.yx.hoststack.center.service.OssConfigService;

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
public class OssConfigServiceImpl extends ServiceImpl<OssConfigMapper, OssConfig> implements OssConfigService {

    
    private final OssConfigMapper ossConfigMapper;

    @Override
    public Page<OssConfig> findPage(OssConfig params) {
        Page<OssConfig> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<OssConfig> query = Wrappers.lambdaQuery(OssConfig.class);
        return ossConfigMapper.selectPage(page, query);
    }

    @Override
    public List<OssConfig> findList(OssConfig params){
        LambdaQueryWrapper<OssConfig> query = Wrappers.lambdaQuery(OssConfig.class);
        return ossConfigMapper.selectList(query);
    }

    @Override
    public OssConfig findById(Long id) {
        return ossConfigMapper.selectById(id);
    }

    @Override
    public boolean insert(OssConfig ossConfig) {
        return save(ossConfig);
    }

    @Override
    public boolean update(OssConfig ossConfig) {
        return updateById(ossConfig);
    }

    @Override
    public int delete(Long id) {
        return ossConfigMapper.deleteById(id);
    }

}