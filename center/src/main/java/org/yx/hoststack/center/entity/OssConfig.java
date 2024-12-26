package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 机房存储信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_oss_config")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OssConfig implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     *  分区标识
     */
    private String region;

    /**
     *  存储类型: OSS/COS/S3/URL
     */
    private String ossType;

    /**
     *  存储配置
     */
    private String ossConfig;

}