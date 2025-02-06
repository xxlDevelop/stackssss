package org.yx.hoststack.center.common.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @Description : x-user definition for transparent transmission
 * @Author : Lee666
 * @Date : 2023/7/22
 * @Version : 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class XUserDTO implements Serializable {
    private static final long serialVersionUID = -7653750121293214951L;
    /**
     * ak or sk for accessing the control console web system
     */
    private String ak;
    /**
     * control console web system access account id
     * or client user id for terminal SDK access scenario
     */
    private String uid;
    /**
     * ak or web account owner tenant id
     */
    private Long tid;
    /**
     * tenant type
     */
    private String tenantType;
    /**
     * control console web system access role
     */
    private String roleId;
    /**
     * client source ip
     */
    private String clientIp;
}