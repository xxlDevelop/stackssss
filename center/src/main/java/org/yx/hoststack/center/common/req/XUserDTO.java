package org.yx.hoststack.center.common.req;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description : x-user definition
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
    @Serial
    private static final long serialVersionUID = -7653750121293214951L;
    private String ak;
    private String uid;
    private Long tid;
    private String tenantType;
    private String roleId;
    private String clientIp;
    private String email;
    private String privyId;
}