package org.yx.hoststack.center.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_storage_file")
public class StorageFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Unique file identifier
     */
    private String fileId;

    /**
     * Bucket name
     */
    private String bucket;

    /**
     * File path/name in bucket
     */
    private String objectKey;

    /**
     * File MD5
     */
    private String md5;

    /**
     * File size
     */
    private Long size;

    /**
     * Tenant ID
     */
    private Long tenantId;

    /**
     * Internal network download URL
     */
    private String localDownloadUrl;

    /**
     * Public network download URL
     */
    private String netDownloadUrl;

    /**
     * Deletion flag: 0-Not deleted, 1-Deleted
     */
    private Integer deleted;

    /**
     * Creation time
     */
    private Timestamp createTime;

    /**
     * Update time
     */
    private Timestamp updateTime;
}