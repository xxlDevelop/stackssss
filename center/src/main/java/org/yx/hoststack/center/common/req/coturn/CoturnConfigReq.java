package org.yx.hoststack.center.common.req.coturn;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Data
public class CoturnConfigReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private Integer id;

    /**
     *  Partition identification
     */
    @NotBlank
    private String region;

    /**
     * COTURNSERVER access base address
     */
    @NotBlank
    private String coturnServerSvc;

    /**
     * COTURNSERVER Access username
     */
    @NotBlank
    private String coturnServerUser;

    /**
     * COTURNSERVER Access password
     */
    @NotBlank
    private String coturnServerPwd;

    @NotBlank
    private String ossType;

    @NotBlank
    private String ossConfig;

}