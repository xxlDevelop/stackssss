package org.yx.hoststack.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.yx.hoststack.center.entity.IdcNetConfig;

import java.util.Set;

/**
 * 机房网络配置
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Mapper
public interface IdcNetConfigMapper extends BaseMapper<IdcNetConfig> {
    Integer existsNetworkConfigs(@Param("localCombinations") Set<String> localCombinations,
                                 @Param("mappingCombinations") Set<String> mappingCombinations);

}