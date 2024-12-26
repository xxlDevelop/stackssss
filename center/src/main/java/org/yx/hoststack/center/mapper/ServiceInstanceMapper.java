package org.yx.hoststack.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.yx.hoststack.center.entity.ServiceInstance;

/**
 * <p>
 * idc服务或者中继节点服务实例信息 Mapper 接口
 * </p>
 *
 * @author Lee666
 * @since 2024-12-25
 */
@Mapper
public interface ServiceInstanceMapper extends BaseMapper<ServiceInstance> {

}
