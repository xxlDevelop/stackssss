package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.mapper.ServiceDetailMapper;
import org.yx.hoststack.center.entity.ServiceDetail;
import org.yx.hoststack.center.service.ServiceDetailService;
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
public class ServiceDetailServiceImpl extends ServiceImpl<ServiceDetailMapper, ServiceDetail> implements ServiceDetailService {

    private ServiceDetailMapper serviceDetailMapper;

    @Override
    public Page<ServiceDetail> findPage(ServiceDetail params) {
        Page<ServiceDetail> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<ServiceDetail> query = Wrappers.lambdaQuery(ServiceDetail.class);
        return serviceDetailMapper.selectPage(page, query);
    }

    @Override
    public List<ServiceDetail> findList(ServiceDetail params){
        LambdaQueryWrapper<ServiceDetail> query = Wrappers.lambdaQuery(ServiceDetail.class);
        return serviceDetailMapper.selectList(query);
    }

    @Override
    public ServiceDetail findById(Long id) {
        return serviceDetailMapper.selectById(id);
    }

    @Override
    public boolean insert(ServiceDetail serviceDetail) {
        return save(serviceDetail);
    }

    @Override
    public boolean update(ServiceDetail serviceDetail) {
        return updateById(serviceDetail);
    }

    @Override
    public int delete(Long id) {
        return serviceDetailMapper.deleteById(id);
    }

}