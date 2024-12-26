package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;
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
@TableName("t_sys_module")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysModule implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  模块ID
     */
    private String moduleId;

    /**
     *  模块名称
     */
    private String moduleName;

    /**
     *  模块适配架构, ARM/X86
     */
    private String moduleArch;

    /**
     *  模块配置信息
     */
    private String moduleConfig;

    /**
     *  模块版本
     */
    private String version;

    /**
     *  下载地址
     */
    private String downloadUrl;

    /**
     *  模块安装包存储路径
     */
    private String storagePath;

    /**
     *  模块文件MD5值
     */
    private String md5;

    /**
     *  创建时间戳
     */
    private Date createAt;

}