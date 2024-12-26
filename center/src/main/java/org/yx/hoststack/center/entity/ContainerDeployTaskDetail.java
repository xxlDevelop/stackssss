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
@TableName("t_container_deploy_task_detail")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContainerDeployTaskDetail implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *   
     */
    private Long deployTaskId;

    /**
     *   
     */
    private String containerId;

    /**
     *  任务状态, START,FAILED,SUCCESS
     */
    private String taskStauts;

    /**
     *  创建时间
     */
    private Date createAt;

    /**
     *  最后修改时间
     */
    private Date lastUpdateAt;

}