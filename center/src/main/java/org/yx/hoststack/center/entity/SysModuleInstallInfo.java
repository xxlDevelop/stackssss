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
 * 系统模块安装信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_sys_module_install_info")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysModuleInstallInfo implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  模块ID
     */
    private String moduleId;

    /**
     *  版本
     */
    private String version;

    /**
     *  HOST或者容器ID
     */
    private String vmHost;

    /**
     *  创建时间
     */
    private Date createAt;

}