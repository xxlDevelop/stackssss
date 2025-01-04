package org.yx.hoststack.center;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.enums.RegisterNodeEnum;
import org.yx.hoststack.center.entity.RegionInfo;
import org.yx.hoststack.center.service.RegionInfoService;
import org.yx.hoststack.center.ws.common.Node;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.awt.event.ActionEvent;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CenterApplicationRunner implements ApplicationRunner {
    public static final String REGION_CACHE_KEY = "regionCache";
    public static final ConcurrentHashMap<String, List<RegionInfo>> regionInfoCacheMap = new ConcurrentHashMap<>();

    private final RegionInfoService regionInfoService;
    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    public static String address;
    public static String hostName;
    public static int port;
    public static Node centerNode;

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
            initRegionInfo();
            getIp();
            centerNode = new Node(hostName, RegisterNodeEnum.CENTER, null);
        } catch (Exception ex) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CenterWsServer)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.CenterWsServer_Region_Initialize)
                    .p(LogFieldConstants.ERR_MSG, ex.getMessage())
                    .e(ex);
        }
    }

    private void initRegionInfo() {
        List<RegionInfo> regionInfos = regionInfoService.list();
        regionInfoCacheMap.put(REGION_CACHE_KEY, regionInfos);
    }

    private void getIp() {
        hostName = nacosDiscoveryProperties.getUsername();
        address = nacosDiscoveryProperties.getIp();
        port = nacosDiscoveryProperties.getPort();
    }
}
