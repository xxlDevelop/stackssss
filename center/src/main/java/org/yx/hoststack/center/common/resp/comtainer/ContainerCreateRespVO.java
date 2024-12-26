package org.yx.hoststack.center.common.resp.comtainer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description : Simple container profile template VO
 * @Author : Lee666
 * @Date : 2023/7/10
 * @Version : 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ContainerCreateRespVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4997508777165216639L;
    /**
     * job id
     */
    private String jobId;

}
