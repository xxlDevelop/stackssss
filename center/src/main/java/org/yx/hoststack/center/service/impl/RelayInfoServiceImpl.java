package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.RelayInfoMapper;
import org.yx.hoststack.center.entity.RelayInfo;
import org.yx.hoststack.center.service.RelayInfoService;

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
public class RelayInfoServiceImpl extends ServiceImpl<RelayInfoMapper, RelayInfo> implements RelayInfoService {

    
    private final RelayInfoMapper relayInfoMapper;

    @Override
    public Page<RelayInfo> findPage(RelayInfo params) {
        Page<RelayInfo> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<RelayInfo> query = Wrappers.lambdaQuery(RelayInfo.class);
        return relayInfoMapper.selectPage(page, query);
    }

    @Override
    public List<RelayInfo> findList(RelayInfo params){
        LambdaQueryWrapper<RelayInfo> query = Wrappers.lambdaQuery(RelayInfo.class);
        return relayInfoMapper.selectList(query);
    }

    @Override
    public RelayInfo findById(Long id) {
        return relayInfoMapper.selectById(id);
    }

    @Override
    public boolean insert(RelayInfo relayInfo) {
        return save(relayInfo);
    }

    @Override
    public boolean update(RelayInfo relayInfo) {
        return updateById(relayInfo);
    }

    @Override
    public int delete(Long id) {
        return relayInfoMapper.deleteById(id);
    }

}