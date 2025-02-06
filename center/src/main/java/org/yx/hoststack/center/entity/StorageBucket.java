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
@TableName("t_storage_bucket")
public class StorageBucket {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Region information
     */
    private String region;

    /**
     * IDC information
     */
    private String idc;

    /**
     * Bucket name
     */
    private String bucket;

    /**
     * Tenant ID
     */
    private Long tenantId;

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