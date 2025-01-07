package org.yx.hoststack.center;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.ws.task.HeartbeatMonitor;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.SpringContextHolder;

import java.io.InputStreamReader;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class CenterApplicationRunner implements ApplicationRunner {
    public static HeartbeatMonitor monitor;
    @Override
    public void run(ApplicationArguments args) {
        try {
            Resource resource = new ClassPathResource("git.properties");
            Properties properties = new Properties();
            properties.load(new InputStreamReader(resource.getInputStream()));
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CenterWsServer)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.CenterWsServer_Region_Initialize)
                    .p("ProjectGitInfo", properties)
                    .i();
            HeartbeatMonitor monitor = SpringContextHolder.getBean(HeartbeatMonitor.class);

            monitor.startMonitor();
        } catch (Exception ex) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CenterWsServer)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.CenterWsServer_Region_Initialize)
                    .p(LogFieldConstants.ERR_MSG, ex.getMessage())
                    .e(ex);
        }
    }
}
