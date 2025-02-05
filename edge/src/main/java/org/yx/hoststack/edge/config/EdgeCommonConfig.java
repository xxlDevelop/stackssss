package org.yx.hoststack.edge.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class EdgeCommonConfig {
    @Value("${runMode}")
    private String runMode;
    @Value("${localIp}")
    private String localIp;

    @Value("${edgeAk}")
    private String edgeAk;
    @Value("${edgeSk}")
    private String edgeSk;

}
