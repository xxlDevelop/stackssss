package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.HostMapper;
import org.yx.hoststack.center.entity.Host;
import org.yx.hoststack.center.service.HostService;

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
public class HostServiceImpl extends ServiceImpl<HostMapper, Host> implements HostService {

    
    private final HostMapper hostMapper;

    @Override
    public Page<Host> findPage(Host params) {
        Page<Host> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<Host> query = Wrappers.lambdaQuery(Host.class);
        return hostMapper.selectPage(page, query);
    }

    @Override
    public List<Host> findList(Host params){
        LambdaQueryWrapper<Host> query = Wrappers.lambdaQuery(Host.class);
        return hostMapper.selectList(query);
    }

    @Override
    public Host findById(Long id) {
        return hostMapper.selectById(id);
    }

    @Override
    public boolean insert(Host host) {
        return save(host);
    }

    @Override
    public boolean update(Host host) {
        return updateById(host);
    }

    @Override
    public int delete(Long id) {
        return hostMapper.deleteById(id);
    }

}