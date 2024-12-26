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
 *  
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_container_deploy_task")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContainerDeployTask implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  大区标识
     */
    private String zone;

    /**
     *  分区标识
     */
    private String region;

    /**
     *  机房标识
     */
    private String idc;

    /**
     *  镜像ID
     */
    private String imageId;

    /**
     *  镜像版本
     */
    private String imageVer;

    /**
     *  容器运行的业务类型, RENDER/AI
     */
    private String bizType;

    /**
     *  任务状态, START,FAILED,SUCCESS
     */
    private String taskStatus;

    /**
     *  下发任务的租户ID
     */
    private Long tenantId;

    /**
     *  创建时间戳
     */
    private Date createAt;

    /**
     *  最后修改时间
     */
    private Date lastUpdateAt;

}