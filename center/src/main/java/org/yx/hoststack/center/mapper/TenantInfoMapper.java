package org.yx.hoststack.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.yx.hoststack.center.entity.TenantInfo;
import org.apache.ibatis.annotations.Mapper;
/**
 * 租户信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 *
 */
@Mapper
public interface TenantInfoMapper extends BaseMapper<TenantInfo> {

}