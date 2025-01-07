package org.yx.hoststack.center.common.req.job;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description : JonDetail - start container DTO
 * @Author : Lee666
 * @Date : 2025/1/6
 * @Version : 1.0
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class JobDetailContainerStartDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5526597945720298856L;
    /**
     * Host resource ID to which the container belongs, created through the hoststack platform, this field is not null
     */
    private String hostId;
    /**
     *  container ID
     */
    private String cid;
}
