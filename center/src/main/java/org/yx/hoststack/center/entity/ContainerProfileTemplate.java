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
 * 容器模板
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_container_profile_template")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContainerProfileTemplate implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  容器虚拟化类型, KVM或DOCKER
     */
    private String containerType;

    /**
     *  容器适用于的业务类型, 包含: RENDER, AI
     */
    private String bizType;

    /**
     *  容器操作系统类型, WINDOWS LINUX或ANDROID
     */
    private String osType;

    /**
     *  硬件架构, ARM/X86
     */
    private String arch;

    /**
     *  模版版本
     */
    private String version;

    /**
     *  模版内容
     */
    private String profile;

    /**
     *  创建时间戳
     */
    private Date createAt;

}