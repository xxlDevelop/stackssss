package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.yx.hoststack.center.common.enums.JobStatusEnum;
import org.yx.hoststack.center.mapper.JobDetailMapper;
import org.yx.hoststack.center.entity.JobDetail;
import org.yx.hoststack.center.service.JobDetailService;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JobDetailServiceImpl extends ServiceImpl<JobDetailMapper, JobDetail> implements JobDetailService {


    private final JobDetailMapper jobDetailMapper;

    @Override
    public Page<JobDetail> findPage(JobDetail params) {
        Page<JobDetail> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<JobDetail> query = Wrappers.lambdaQuery(JobDetail.class);
        return jobDetailMapper.selectPage(page, query);
    }

    @Override
    public List<JobDetail> findList(JobDetail params) {
        LambdaQueryWrapper<JobDetail> query = Wrappers.lambdaQuery(JobDetail.class);
        return jobDetailMapper.selectList(query);
    }

    @Override
    public JobDetail findById(Long id) {
        return jobDetailMapper.selectById(id);
    }

    @Override
    public boolean insert(JobDetail jobDetail) {
        return save(jobDetail);
    }

    @Override
    public boolean update(JobDetail jobDetail) {
        return updateById(jobDetail);
    }

    @Override
    public int delete(Long id) {
        return jobDetailMapper.deleteById(id);
    }

    @Override
    public long countByStatus(String jobId, List<String> jobStatus) {
        return this.count(Wrappers.lambdaQuery(JobDetail.class)
                .eq(JobDetail::getJobId, jobId)
                .in(JobDetail::getJobStatus, jobStatus));
    }
}