package org.yx.hoststack.center.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.enums.DeletedEnum;
import org.yx.hoststack.center.common.exception.StorageBucketException;
import org.yx.hoststack.center.common.req.storage.CreateBucketReq;
import org.yx.hoststack.center.common.req.storage.StorageIdcBucketListReq;
import org.yx.hoststack.center.common.resp.PageResp;
import org.yx.hoststack.center.common.resp.storage.BucketListResp;
import org.yx.hoststack.center.common.utils.LoginUtil;
import org.yx.hoststack.center.entity.StorageBucket;
import org.yx.hoststack.center.mapper.StorageBucketMapper;
import org.yx.hoststack.center.service.StorageBucketService;
import org.yx.lib.utils.constant.CommonConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.StringUtil;

import java.util.List;
import java.util.stream.Collectors;

import static org.yx.hoststack.center.common.enums.SysCode.*;

/**
 * @author lyc
 * @since 2025-02-05 16:00:24
 */
@Service
@Slf4j
public class StorageBucketServiceImpl extends ServiceImpl<StorageBucketMapper, StorageBucket> implements StorageBucketService {

    @Autowired
    private StorageBucketMapper storageBucketMapper;

    @Override
    public Page<StorageBucket> findPage(StorageBucket params) {
        Page<StorageBucket> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<StorageBucket> query = Wrappers.lambdaQuery(StorageBucket.class);
        return storageBucketMapper.selectPage(page, query);
    }

    @Override
    public List<StorageBucket> findList(StorageBucket params) {
        LambdaQueryWrapper<StorageBucket> query = Wrappers.lambdaQuery(StorageBucket.class);
        return storageBucketMapper.selectList(query);
    }

    @Override
    public StorageBucket findById(Long id) {
        return storageBucketMapper.selectById(id);
    }

    @Override
    public boolean insert(StorageBucket storageBucket) {
        return save(storageBucket);
    }

    @Override
    public boolean update(StorageBucket storageBucket) {
        return updateById(storageBucket);
    }

    @Override
    public int delete(Long id) {
        return storageBucketMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<?> createBucket(CreateBucketReq request) {
        if (StringUtil.isBlank(request.getRegion()) && StringUtil.isBlank(request.getIdc())) {
            return R.failed(x00000530.getValue(), x00000530.getMsg());
        }
        try {
            Long tenantId = LoginUtil.getTenantId();
            validateBucketNotExists(request.getBucket(), tenantId);

            StorageBucket bucket = StorageBucket.builder()
                    .region(request.getRegion())
                    .idc(request.getIdc())
                    .bucket(request.getBucket())
                    .tenantId(tenantId)
                    .deleted(DeletedEnum.NOT_DELETED.getCode())
                    .build();
            storageBucketMapper.insert(bucket);
            return R.ok();
        } catch (StorageBucketException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return R.failed(e.getCode(), e.getMessage());
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.STORAGE_IDC_EVENT)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.STORAGE_IDC_CREATE_BUCKET_FAILED)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(request))
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(request))
                    .p(LogFieldConstants.TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                    .e(e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return R.failed(x00000532.getValue(), x00000532.getMsg());
        }
    }

    private void validateBucketNotExists(String bucketName, Long tenantId) {
        Long count = storageBucketMapper.selectCount(
                new LambdaQueryWrapper<StorageBucket>()
                        .eq(StorageBucket::getBucket, bucketName)
                        .eq(StorageBucket::getTenantId, tenantId)
                        .eq(StorageBucket::getDeleted, 0)
        );

        if (count > 0) {
            throw new StorageBucketException(x00000531.getValue(), String.format(x00000531.getMsg(), bucketName));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<?> deleteBucket(CreateBucketReq request) {
        if (StringUtil.isBlank(request.getRegion()) && StringUtil.isBlank(request.getIdc())) {
            return R.failed(x00000530.getValue(), x00000530.getMsg());
        }

        try {
            // Find bucket
            LambdaQueryWrapper<StorageBucket> wrapper = new LambdaQueryWrapper<StorageBucket>()
                    .eq(StorageBucket::getBucket, request.getBucket())
                    .eq(StorageBucket::getTenantId, LoginUtil.getTenantId())
                    .eq(StorageBucket::getDeleted, DeletedEnum.NOT_DELETED.getCode());

            if (StringUtil.isNotBlank(request.getRegion())) {
                wrapper.eq(StorageBucket::getRegion, request.getRegion());
            }
            if (StringUtil.isNotBlank(request.getIdc())) {
                wrapper.eq(StorageBucket::getIdc, request.getIdc());
            }

            StorageBucket bucket = storageBucketMapper.selectOne(wrapper);
            if (bucket == null) {
                return R.failed(x00000534.getValue(),
                        String.format(x00000534.getMsg(), request.getBucket()));
            }

            // Logical delete
            bucket.setDeleted(DeletedEnum.DELETED.getCode());
            storageBucketMapper.updateById(bucket);
            return R.ok();
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.STORAGE_IDC_EVENT)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.STORAGE_IDC_DELETE_BUCKET_FAILED)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(request))
                    .p(LogFieldConstants.TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                    .e(e);
            return R.failed(x00000534.getValue(), x00000534.getMsg());
        }
    }

    @Override
    public R<PageResp<BucketListResp>> list(StorageIdcBucketListReq request) {
        try {
            Long tenantId = LoginUtil.getTenantId();
            // Build query wrapper
            LambdaQueryWrapper<StorageBucket> wrapper = new LambdaQueryWrapper<StorageBucket>()
                    .eq(StorageBucket::getTenantId, tenantId)
                    .eq(StorageBucket::getDeleted, DeletedEnum.NOT_DELETED)
                    // Add optional conditions
                    .eq(StringUtil.isNotBlank(request.getRegion()),
                            StorageBucket::getRegion, request.getRegion())
                    .eq(StringUtil.isNotBlank(request.getIdc()),
                            StorageBucket::getIdc, request.getIdc())
                    // Order by creation time desc
                    .orderByDesc(StorageBucket::getCreateTime);

            // Execute paginated query
            Page<StorageBucket> page = new Page<>(request.getCurrent(), request.getSize());
            Page<StorageBucket> resultPage = storageBucketMapper.selectPage(page, wrapper);

            // Convert to response
            List<BucketListResp> records = resultPage.getRecords().stream()
                    .map(BucketListResp::from)
                    .collect(Collectors.toList());
            PageResp<BucketListResp> resultData = new PageResp<>();
            resultData.setCurrent(request.getCurrent());
            resultData.setSize(request.getSize());
            resultData.setTotal(page.getTotal());
            resultData.setPages(page.getPages());
            resultData.setRecords(records);
            return R.ok(resultData);
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.STORAGE_IDC_EVENT)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.STORAGE_IDC_LIST_BUCKETS_FAILED)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(request))
                    .p(LogFieldConstants.TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                    .e(e);
            return R.failed(x00000535.getValue(), x00000535.getMsg());
        }
    }


}