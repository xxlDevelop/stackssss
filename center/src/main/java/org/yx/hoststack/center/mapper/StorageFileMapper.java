package org.yx.hoststack.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.yx.hoststack.center.common.resp.storage.StorageFileListResp;
import org.yx.hoststack.center.entity.StorageFile;

/**
 * 存储文件表
 *
 * @author lyc
 * @since 2025-02-05 16:00:24
 */
@Mapper
public interface StorageFileMapper extends BaseMapper<StorageFile> {
    Page<StorageFileListResp> listFiles(Page<?> page,
                                        @Param("tenantId") Long tenantId,
                                        @Param("bucket") String bucket,
                                        @Param("region") String region,
                                        @Param("idc") String idc);
}