package org.yx.hoststack.edge.client.controller.jobs;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.yx.hoststack.edge.common.CacheKeyConstants;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class JobCacheService {
    private final RedissonClient redissonClient;

    public void createJob(JobCacheData jobCacheData) {
        String cacheKey = String.format(CacheKeyConstants.RegionJob, jobCacheData.getJobDetailId());
        redissonClient.getBucket(cacheKey).set(jobCacheData.toString(), Duration.ofHours(72));
    }

    public JobCacheData getJob(String jobFullId) {
        String cacheKey = String.format(CacheKeyConstants.RegionJob, jobFullId);
        Object cacheData = redissonClient.getBucket(cacheKey).get();
        if (cacheData == null) {
            return null;
        }
        return JSON.parseObject(cacheData.toString(), JobCacheData.class);
    }

    public void deleteJob(String jobFullId) {
        String cacheKey = String.format(CacheKeyConstants.RegionJob, jobFullId);
        redissonClient.getBucket(cacheKey).delete();
    }
}
