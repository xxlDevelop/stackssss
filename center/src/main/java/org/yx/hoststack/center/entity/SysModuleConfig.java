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
 * 模块安装配置信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_sys_module_config")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysModuleConfig implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *   
     */
    private String moduleId;

    /**
     *  模块安装关系ID
     */
    private Long installRelId;

    /**
     *  模块安装配置
     */
    private String moduleConfig;

}