package org.yx.hoststack.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.yx.hoststack.center.entity.AgentSession;
import org.yx.hoststack.center.entity.ContainerDeployTaskDetail;

@Mapper
public interface AgentSessionMapper  extends BaseMapper<AgentSession> {
}