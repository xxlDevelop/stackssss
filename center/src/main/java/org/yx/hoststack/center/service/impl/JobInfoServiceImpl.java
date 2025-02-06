package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.entity.JobInfo;
import org.yx.hoststack.center.mapper.JobInfoMapper;
import org.yx.hoststack.center.service.JobInfoService;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JobInfoServiceImpl extends ServiceImpl<JobInfoMapper, JobInfo> implements JobInfoService {


    private final JobInfoMapper jobInfoMapper;

    @Override
    public Page<JobInfo> findPage(JobInfo params) {
        Page<JobInfo> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<JobInfo> query = Wrappers.lambdaQuery(JobInfo.class);
        return jobInfoMapper.selectPage(page, query);
    }

    @Override
    public List<JobInfo> findList(JobInfo params) {
        LambdaQueryWrapper<JobInfo> query = Wrappers.lambdaQuery(JobInfo.class);
        return jobInfoMapper.selectList(query);
    }

    @Override
    public JobInfo findById(Long id) {
        return jobInfoMapper.selectById(id);
    }

    @Override
    public boolean insert(JobInfo jobInfo) {
        return save(jobInfo);
    }

    @Override
    public boolean update(JobInfo jobInfo) {
        return updateById(jobInfo);
    }

    @Override
    public int delete(Long id) {
        return jobInfoMapper.deleteById(id);
    }

}