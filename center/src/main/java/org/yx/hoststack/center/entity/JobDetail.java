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
@TableName("t_job_detail")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobDetail implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  任务ID
     */
    private String jobId;

    /**
     *  任务执行目标所属宿主机ID或者容器ID
     */
    private String jobHost;

    /**
     *  任务执行状态, WAIT: 等待执行, PROCESSING: 执行中, FAIL:执行失败, SUCCESS:执行成功
     */
    private String jobStatus;

    /**
     *  任务执行进度, 范围:[0,100]
     */
    private Integer jobProcess;

    /**
     *  任务结果
     */
    private String jobResult;

    /**
     *  任务运行时间, 单位秒
     */
    private Integer runTime;

    /**
     *  创建时间戳
     */
    private Date createAt;

    /**
     *  修后修时间戳
     */
    private Date lastUpdateAt;

}