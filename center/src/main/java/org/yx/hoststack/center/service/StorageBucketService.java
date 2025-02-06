package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.common.req.storage.CreateBucketReq;
import org.yx.hoststack.center.common.req.storage.StorageIdcBucketListReq;
import org.yx.hoststack.center.common.resp.PageResp;
import org.yx.hoststack.center.common.resp.storage.BucketListResp;
import org.yx.hoststack.center.entity.StorageBucket;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * @author lyc
 * @since 2025-02-05 16:00:24
 */
public interface StorageBucketService extends IService<StorageBucket> {

    Page<StorageBucket> findPage(StorageBucket params);

    List<StorageBucket> findList(StorageBucket params);

    StorageBucket findById(Long id);

    boolean insert(StorageBucket storageBucket);

    boolean update(StorageBucket storageBucket);

    int delete(Long id);

    /**
     * Create a new storage bucket
     *
     * @param request bucket creation request
     */
    R<?> createBucket(CreateBucketReq request);

    /**
     * Delete storage bucket
     *
     * @param request delete request
     * @return operation result
     */
    R<?> deleteBucket(CreateBucketReq request);

    /**
     * List storage buckets with pagination
     *
     * @param request query parameters with pagination
     * @return page of bucket info
     */
    R<PageResp<BucketListResp>> list(StorageIdcBucketListReq request);
}