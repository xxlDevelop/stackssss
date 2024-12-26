package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.JobInfo;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface JobInfoService extends IService<JobInfo> {

    Page<JobInfo> findPage(JobInfo params);

    List<JobInfo> findList(JobInfo params);

    JobInfo findById(Long id);

    boolean insert(JobInfo jobInfo);

    boolean update(JobInfo jobInfo);

    int delete(Long id);

}