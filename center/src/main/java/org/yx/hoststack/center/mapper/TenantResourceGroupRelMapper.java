package org.yx.hoststack.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.yx.hoststack.center.entity.TenantResourceGroupRel;
import org.apache.ibatis.annotations.Mapper;
/**
 * 资源分组与资源绑定关系表
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 *
 */
@Mapper
public interface TenantResourceGroupRelMapper extends BaseMapper<TenantResourceGroupRel> {

}