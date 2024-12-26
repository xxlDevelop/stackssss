package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.entity.ServiceInstance;
import org.yx.hoststack.center.mapper.ServiceInstanceMapper;
import org.yx.hoststack.center.service.ServiceInstanceService;

/**
 * <p>
 * idc服务或者中继节点服务实例信息 服务实现类
 * </p>
 *
 * @author Lee666
 * @since 2024-12-25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceInstanceServiceImpl extends ServiceImpl<ServiceInstanceMapper, ServiceInstance> implements ServiceInstanceService {

}
