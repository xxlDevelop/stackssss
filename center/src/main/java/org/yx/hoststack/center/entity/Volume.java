package org.yx.hoststack.center.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * volume
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_volume")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Volume implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private String volumeId;

    /**
     * Sparse size of data volume, unit: KB
     */
    private Long volumeSize;

    /**
     * Data volume disk type, LOCAL: Local disk, REMOTE:  Network disk
     */
    private String diskType;

    /**
     * Storage volume type: base or user
     */
    private String volumeType;

    /**
     * The metadata address to be downloaded when creating a non empty storage volume
     */
    private String downloadUrl;

    /**
     * Create timestamp
     */
    private Timestamp createAt;

}