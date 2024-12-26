package org.yx.hoststack.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.yx.hoststack.center.entity.JobInfo;
import org.apache.ibatis.annotations.Mapper;
/**
 * 任务信息表
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 *
 */
@Mapper
public interface JobInfoMapper extends BaseMapper<JobInfo> {

}