package org.yx.hoststack.center.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.req.storage.StorageIdcObjectListReq;
import org.yx.hoststack.center.common.resp.PageResp;
import org.yx.hoststack.center.common.resp.storage.StorageFileListResp;
import org.yx.hoststack.center.common.utils.LoginUtil;
import org.yx.hoststack.center.entity.StorageFile;
import org.yx.hoststack.center.mapper.StorageFileMapper;
import org.yx.hoststack.center.service.StorageFileService;
import org.yx.lib.utils.constant.CommonConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;

import java.util.List;

import static org.yx.hoststack.center.common.enums.SysCode.*;

/**
 * @author lyc
 * @since 2025-02-05 16:00:24
 */
@Service
@Slf4j
public class StorageFileServiceImpl extends ServiceImpl<StorageFileMapper, StorageFile> implements StorageFileService {

    @Autowired
    private StorageFileMapper storageFileMapper;

    @Override
    public Page<StorageFile> findPage(StorageFile params) {
        Page<StorageFile> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<StorageFile> query = Wrappers.lambdaQuery(StorageFile.class);
        return storageFileMapper.selectPage(page, query);
    }

    @Override
    public List<StorageFile> findList(StorageFile params) {
        LambdaQueryWrapper<StorageFile> query = Wrappers.lambdaQuery(StorageFile.class);
        return storageFileMapper.selectList(query);
    }

    @Override
    public StorageFile findById(Long id) {
        return storageFileMapper.selectById(id);
    }

    @Override
    public boolean insert(StorageFile storageFile) {
        return save(storageFile);
    }

    @Override
    public boolean update(StorageFile storageFile) {
        return updateById(storageFile);
    }

    @Override
    public int delete(Long id) {
        return storageFileMapper.deleteById(id);
    }

    @Override
    public R<PageResp<StorageFileListResp>> listFiles(StorageIdcObjectListReq request) {
        try {
            long tenantId = LoginUtil.getTenantId();
            if (StringUtil.isBlank(request.getRegion()) && StringUtil.isBlank(request.getIdc())) {
                return R.failed(x00000530.getValue(), x00000530.getMsg());
            }
            if (StringUtil.isBlank(request.getBucket())) {
                return R.failed(x00000409.getValue(), x00000409.getMsg());
            }

            // Create page objects
            Page<StorageFileListResp> page = new Page<>(request.getCurrent(), request.getSize());

            // Execute paging query
            Page<StorageFileListResp> result = storageFileMapper.listFiles(
                    page,
                    tenantId,
                    request.getBucket(),
                    request.getRegion(),
                    request.getIdc()
            );
            PageResp<StorageFileListResp> resultData = new PageResp<>();
            resultData.setCurrent(request.getCurrent());
            resultData.setSize(request.getSize());
            resultData.setTotal(page.getTotal());
            resultData.setPages(page.getPages());
            resultData.setRecords(result.getRecords());
            return R.ok(resultData);
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.STORAGE_FILE_EVENT)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.STORAGE_FILE_LIST_FAILED)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(request))
                    .p(LogFieldConstants.TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                    .e(e);
            return R.failed(x00000536.getValue(), x00000536.getMsg());
        }
    }

}