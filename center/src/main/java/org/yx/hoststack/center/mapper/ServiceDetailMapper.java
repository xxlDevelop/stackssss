package org.yx.hoststack.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.yx.hoststack.center.entity.ServiceDetail;
import org.apache.ibatis.annotations.Mapper;
/**
 * IDC服务或者中继节点服务信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 *
 */
@Mapper
public interface ServiceDetailMapper extends BaseMapper<ServiceDetail> {

}