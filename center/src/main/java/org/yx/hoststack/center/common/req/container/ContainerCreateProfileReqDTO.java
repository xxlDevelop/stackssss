package org.yx.hoststack.center.common.req.container;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description : Container create profile context request DTO
 * @Author : Lee666
 * @Date : 2024/12/25
 * @Version : 1.0
 */
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class ContainerCreateProfileReqDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 8024997861883693314L;
    /**
     * Number of CPU cores
     */
    @NotNull(message = "vCpu cannot be empty")
    @JsonProperty("vCpu")
    private Integer vCpu;
    /**
     * Memory, unit MB
     */
    @NotBlank(message = "memory cannot be empty")
    private String memory;
    /**
     * Operating System Type
     */
    @NotBlank(message = "osType cannot be empty")
    private String osType;
    /**
     * Network card working mode: bridge、nat
     */
    @NotBlank(message = "netMode cannot be empty")
    private String netMode;
}
