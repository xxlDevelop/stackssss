package org.yx.hoststack.center.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * IDC服务或者中继节点服务信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostServiceDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String serviceId = "";

    private String hostId = "";

    private String idcSid = "";

    private String relaySid = "";

    private String type;

    private String zone = "";

    private String region = "";

    private HostServiceDetailDTO parent;

}