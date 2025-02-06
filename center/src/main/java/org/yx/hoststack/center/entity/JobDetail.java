package org.yx.hoststack.center.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_job_detail")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Subtask ID (jobId + "-" + targetId(containerId or volumeId or ...))
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String jobDetailId;

    /**
     * jobInfo id
     */
    private String jobId;

    /**
     * Host ID/container ID/storage volume ID to which the task execution target belongs
     */
    private String jobHost;

    /**
     * Task execution status, wait:  Waiting for execution, processing:  During execution, fail: Execution failed, success: Execution successful
     */
    private String jobStatus;

    /**
     * Task execution progress, scope: [0-100]
     */
    private Integer jobProgress;

    /**
     * Task parameters
     */
    private String jobParams;

    /**
     * task outcomes
     */
    private String jobResult;

    /**
     * 任务执行目标所属大区标识
     */
    private String zone;

    /**
     * 任务执行目标所属分区
     */
    private String region;

    /**
     * 任务执行目标所属的RELAY标识
     */
    private String relay;

    /**
     * 任务执行目标所属IDC机房
     */
    private String idc;

    /**
     * 任务执行目标所属IDC服务标识
     */
    private String idcSid;

    /**
     * 任务执行目标所属的RELAY服务标识
     */
    private String relaySid;

    /**
     * Task running time, in seconds
     */
    private Integer runTime;

    /**
     * Create timestamp
     */
    private Timestamp createAt;

    /**
     * Post repair timestamp
     */
    private Timestamp lastUpdateAt;

}