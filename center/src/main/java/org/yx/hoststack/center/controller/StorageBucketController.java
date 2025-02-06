package org.yx.hoststack.center.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yx.hoststack.center.common.constant.RequestMappingBase;
import org.yx.hoststack.center.common.req.storage.CreateBucketReq;
import org.yx.hoststack.center.common.req.storage.StorageIdcBucketListReq;
import org.yx.hoststack.center.common.req.storage.StorageIdcObjectListReq;
import org.yx.hoststack.center.common.resp.PageResp;
import org.yx.hoststack.center.common.resp.storage.BucketListResp;
import org.yx.hoststack.center.common.resp.storage.StorageFileListResp;
import org.yx.hoststack.center.service.StorageBucketService;
import org.yx.hoststack.center.service.StorageFileService;
import org.yx.lib.utils.util.R;

/**
 * 存储桶表
 *
 * @author lyc
 * @since 2025-02-05 16:00:24
 */
@RestController
@RequestMapping(RequestMappingBase.admin + RequestMappingBase.storageIdc)
@RequiredArgsConstructor
public class StorageBucketController {

    private final StorageBucketService storageBucketService;
    private final StorageFileService storageFileService;


    @PostMapping("/bucket/create")
    public R<?> createBucket(
            @Validated @RequestBody CreateBucketReq request) {
        return storageBucketService.createBucket(request);
    }

    @PostMapping("/bucket/delete")
    public R<?> deleteBucket(
            @Validated @RequestBody CreateBucketReq request) {
        return storageBucketService.deleteBucket(request);
    }

    @PostMapping("/bucket/list")
    public R<PageResp<BucketListResp>> list(@RequestBody @Validated StorageIdcBucketListReq request) {
        return storageBucketService.list(request);
    }

    @PostMapping("/object/list")
    public R<PageResp<StorageFileListResp>> listFiles(@Validated @RequestBody StorageIdcObjectListReq request) {
        return storageFileService.listFiles(request);
    }


}