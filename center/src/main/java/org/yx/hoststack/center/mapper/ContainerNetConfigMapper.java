package org.yx.hoststack.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.yx.hoststack.center.entity.ContainerNetConfig;

/**
 * <p>
 * 容器IP配置信息表 Mapper 接口
 * </p>
 *
 * @author Lee666
 * @since 2024-12-25
 */
@Mapper
public interface ContainerNetConfigMapper extends BaseMapper<ContainerNetConfig> {

}
