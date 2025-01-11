package org.yx.hoststack.center;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.ws.CenterServer;
import org.yx.hoststack.center.ws.common.ConsistentHashing;
import org.yx.hoststack.center.ws.common.Node;
import org.yx.hoststack.center.ws.heartbeat.HeartbeatMonitor;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.SpringContextHolder;

import java.io.InputStreamReader;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class CenterApplicationRunner implements ApplicationRunner {
    public static final ConsistentHashing consistentHash = new ConsistentHashing(10);

    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    public static String address;
    public static String hostName;
    public static int port;
    public final HeartbeatMonitor monitor;

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

            hostName = nacosDiscoveryProperties.getUsername();

            address = nacosDiscoveryProperties.getIp();

            port = nacosDiscoveryProperties.getPort();

            for (int i = 0; i < 10; i++) {
                consistentHash.addShard(String.format("hoststack_shard_%s", i));
            }


        } catch (Exception ex) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CenterWsServer)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.CenterWsServer_Region_Initialize)
                    .p(LogFieldConstants.ERR_MSG, ex.getMessage())
                    .e(ex);
        }
    }
}
