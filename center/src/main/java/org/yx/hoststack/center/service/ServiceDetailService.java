package org.yx.hoststack.center.service;

import org.yx.hoststack.center.entity.ServiceDetail;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
/**
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
public interface ServiceDetailService extends IService<ServiceDetail> {

    Page<ServiceDetail> findPage(ServiceDetail params);

    List<ServiceDetail> findList(ServiceDetail params);

    ServiceDetail findById(Long id);

    boolean insert(ServiceDetail serviceDetail);

    boolean update(ServiceDetail serviceDetail);

    int delete(Long id);

}