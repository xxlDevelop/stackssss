package org.yx.hoststack.edge;

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.yx.hoststack.edge.client.EdgeClient;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.config.EdgeClientConfig;
import org.yx.hoststack.edge.config.EdgeCommonConfig;
import org.yx.hoststack.edge.config.EdgeServerConfig;
import org.yx.hoststack.edge.server.RunMode;
import org.yx.hoststack.edge.server.ws.EdgeServer;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

/**
 * @author EDY
 */
@SpringBootApplication
public class EdgeApplication {
    public static ApplicationContext Context = null;

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale.setDefault(Locale.ENGLISH);
        SpringApplication springApplication = new SpringApplication(EdgeApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.REACTIVE);
        ConfigurableApplicationContext context = springApplication.run(args);
        Context = context;
        start(context);
    }

    private static void start(ConfigurableApplicationContext context) {
        EdgeCommonConfig edgeCommonConfig = context.getBean(EdgeCommonConfig.class);
        EdgeServerConfig edgeServerConfig = context.getBean(EdgeServerConfig.class);
        EdgeClientConfig edgeClientConfig = context.getBean(EdgeClientConfig.class);
        // init service info
        EdgeContext.ServiceIp = edgeCommonConfig.getLocalIp();
        EdgeContext.RunMode = edgeCommonConfig.getRunMode();
        EdgeContext.HttpHost = "http://" + edgeCommonConfig.getLocalIp() + ":" + edgeCommonConfig.getServerPort();
        if (EdgeContext.RunMode.equals(RunMode.IDC)) {
            EdgeContext.IdcServiceId = DigestUtils.md5Hex(edgeCommonConfig.getLocalIp().getBytes(StandardCharsets.UTF_8));
        } else {
            EdgeContext.RelayServiceId = DigestUtils.md5Hex(edgeCommonConfig.getLocalIp().getBytes(StandardCharsets.UTF_8));
        }
        setProjectVersion();
        KvLogger.instance(EdgeApplication.class)
                .p(LogFieldConstants.EVENT, "EdgeInit")
                .p(LogFieldConstants.ACTION, "InitApplication")
                .p("RunMode", EdgeContext.RunMode)
                .p("EdgeCommonConfig", JSON.toJSONString(edgeCommonConfig))
                .p("EdgeServerConfig", JSON.toJSONString(edgeServerConfig))
                .p("EdgeClientConfig", JSON.toJSONString(edgeClientConfig))
                .i();
        startWsServer(context);
        startClient(context);
    }

    private static void startWsServer(ConfigurableApplicationContext context) {
        EdgeServer edgeServer = context.getBean(EdgeServer.class);
        edgeServer.start();
    }

    private static void startClient(ConfigurableApplicationContext context) {
        EdgeClient edgeClient = context.getBean(EdgeClient.class);
        edgeClient.start();
    }

    private static void setProjectVersion() {
        String gitVersion = "unKnow";
        String gitBranch = "unKnow";
        try {
            Resource resource = new ClassPathResource("git.properties");
            Properties properties = new Properties();
            try (InputStream inputStream = resource.getInputStream()) {
                try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                    properties.load(inputStreamReader);
                    gitVersion = properties.getProperty("git.commit.id.abbrev");
                    gitBranch = properties.getProperty("git.branch");
                }
            }
        } catch (Exception ignored) {
        }
        EdgeContext.ProjectVersion = gitBranch + "." + gitVersion;
    }
}
