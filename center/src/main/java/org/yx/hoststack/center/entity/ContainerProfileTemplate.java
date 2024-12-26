package org.yx.hoststack.center.entity;

import java.io.Serial;
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
 * container profile template table entity
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

    @Serial
    private static final long serialVersionUID = 7971224576699871908L;
    /**
     *  ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * The business types applicable to the image: render/ai
     */
    private String bizType;
    /**
     * Mirror operating system type: windows/linux/android
     */
    private String osType;
    /**
     * Mirror virtualization type: docker/kvm
     */
    private String containerType;
    /**
     * Hardware architecture: arm/x86
     */
    private String arch;

    /**
     *  Template Content
     */
    private String profile;

    /**
     *  create time
     */
    private Date createAt;

}