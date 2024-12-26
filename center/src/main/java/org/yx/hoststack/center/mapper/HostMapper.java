package org.yx.hoststack.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.yx.hoststack.center.entity.Host;
import org.apache.ibatis.annotations.Mapper;
/**
 * AGENT信息表,存储HOSTAGENT和CONTAINERAGENT信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 *
 */
@Mapper
public interface HostMapper extends BaseMapper<Host> {

}