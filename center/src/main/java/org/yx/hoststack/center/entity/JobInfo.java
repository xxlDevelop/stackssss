package org.yx.hoststack.center.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 任务信息表
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_job_info")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private String jobId;

    /**
     * 任务类型:
     * VOLUMN,
     * CONTAINER,
     * FILE,
     * MODULE
     */
    private String jobType;

    /**
     * 任务子类型
     * VOLUMN: CREATE, MOUNT, UNMOUNT, DELETE
     * CONTAINER: CREATE, DELETE, UPGRADE, START, STOP, REBOOT,RESET, REBOOT, EXEC_SCRIPT, INSTALL_APP,UNINSTALL_APP
     * FILE: PULL, RECOVER
     * MODULE: INSTALL, UNINSTALL, UPGRADE
     */
    private String jobSubType;

    /**
     * 任务执行状态, WAIT: 等待执行, PROCESSING: 执行中, FAIL:执行失败, SUCCESS:执行成功
     */
    private String jobStatus;

    /**
     * 任务执行进度, 范围:[0,100]
     */
    private Integer jobProcess;

    /**
     * 任务执行目标所属大区标识
     */
    private String zone;

    /**
     * 任务执行目标所属分区
     */
    private String region;

    /**
     * 任务执行目标所属IDC机房
     */
    private String idc;

    /**
     * 子任务个数
     */
    private Integer jobDetailNum;

    /**
     * 下发任务的租户ID
     */
    private Long tenantId;

    /**
     * 任务参数
     */
    private String jobParams;

    /**
     * 扩展参数
     */
    private String externalParams;

    /**
     * 执行时间, 单位秒
     */
    private Long runTime;

    /**
     * 父级任务ID
     */
    private String parentJobId;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 最后修改时间
     */
    private Date lastUpdateAt;

}