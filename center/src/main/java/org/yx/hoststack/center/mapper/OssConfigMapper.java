package org.yx.hoststack.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.yx.hoststack.center.entity.OssConfig;
import org.apache.ibatis.annotations.Mapper;
/**
 * 机房存储信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 *
 */
@Mapper
public interface OssConfigMapper extends BaseMapper<OssConfig> {

    @Select("SELECT * FROM t_oss_config WHERE region = #{region}")
    OssConfig findByRegion(String region);
}