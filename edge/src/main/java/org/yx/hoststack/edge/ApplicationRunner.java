//package org.yx.hoststack.edge;
//
//import com.alibaba.fastjson.JSON;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.stereotype.Component;
//import org.yx.hoststack.edge.server.ws.EdgeServer;
//import org.yx.hoststack.edge.server.EdgeServerConfig;
//import org.yx.lib.utils.logger.KvLogger;
//import org.yx.lib.utils.logger.LogFieldConstants;
//
//import java.util.TimeZone;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class ApplicationRunner implements org.springframework.boot.ApplicationRunner {
//
//    private final EdgeServer edgeServer;
//    private final EdgeServerConfig edgeServerConfig;
//
//    @PostConstruct
//    void started() {
//        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
//    }
//
//    @Override
//    public void run(ApplicationArguments args) {
//        KvLogger.instance(this)
//                .p(LogFieldConstants.EVENT, "EdgeInit")
//                .p(LogFieldConstants.ACTION, "InitApplication")
//                .p("RunMode", edgeServerConfig.getRunMode())
//                .p("EdgeConfig", JSON.toJSONString(edgeServerConfig))
//                .i();
//        edgeServer.start();
//    }
//}
