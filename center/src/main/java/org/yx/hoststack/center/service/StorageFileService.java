package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.common.req.storage.StorageIdcObjectListReq;
import org.yx.hoststack.center.common.resp.PageResp;
import org.yx.hoststack.center.common.resp.storage.StorageFileListResp;
import org.yx.hoststack.center.entity.StorageFile;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * @author lyc
 * @since 2025-02-05 16:00:24
 */
public interface StorageFileService extends IService<StorageFile> {

    Page<StorageFile> findPage(StorageFile params);

    List<StorageFile> findList(StorageFile params);

    StorageFile findById(Long id);

    boolean insert(StorageFile storageFile);

    boolean update(StorageFile storageFile);

    int delete(Long id);

    R<PageResp<StorageFileListResp>> listFiles(StorageIdcObjectListReq request);

}