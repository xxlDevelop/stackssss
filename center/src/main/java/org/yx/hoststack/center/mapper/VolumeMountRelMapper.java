package org.yx.hoststack.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.yx.hoststack.center.entity.VolumeMountRel;
import org.apache.ibatis.annotations.Mapper;
/**
 * 存储卷挂载关系
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 *
 */
@Mapper
public interface VolumeMountRelMapper extends BaseMapper<VolumeMountRel> {

}