package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.RegionInfoMapper;
import org.yx.hoststack.center.entity.RegionInfo;
import org.yx.hoststack.center.service.RegionInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

/**
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RegionInfoServiceImpl extends ServiceImpl<RegionInfoMapper, RegionInfo> implements RegionInfoService {

    private RegionInfoMapper regionInfoMapper;

    @Override
    public Page<RegionInfo> findPage(RegionInfo params) {
        Page<RegionInfo> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<RegionInfo> query = Wrappers.lambdaQuery(RegionInfo.class);
        return regionInfoMapper.selectPage(page, query);
    }

    @Override
    public List<RegionInfo> findList(RegionInfo params){
        LambdaQueryWrapper<RegionInfo> query = Wrappers.lambdaQuery(RegionInfo.class);
        return regionInfoMapper.selectList(query);
    }

    @Override
    public RegionInfo findById(Long id) {
        return regionInfoMapper.selectById(id);
    }

    @Override
    public boolean insert(RegionInfo regionInfo) {
        return save(regionInfo);
    }

    @Override
    public boolean update(RegionInfo regionInfo) {
        return updateById(regionInfo);
    }

    @Override
    public int delete(Long id) {
        return regionInfoMapper.deleteById(id);
    }

}