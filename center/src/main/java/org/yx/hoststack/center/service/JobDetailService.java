package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.common.enums.JobStatusEnum;
import org.yx.hoststack.center.entity.JobDetail;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface JobDetailService extends IService<JobDetail> {

    Page<JobDetail> findPage(JobDetail params);

    List<JobDetail> findList(JobDetail params);

    JobDetail findById(Long id);

    boolean insert(JobDetail jobDetail);

    boolean update(JobDetail jobDetail);

    int delete(Long id);

    long countByStatus(String jobId, List<String> jobStatus);
}