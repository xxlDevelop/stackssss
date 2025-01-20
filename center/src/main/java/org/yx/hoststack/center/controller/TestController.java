package org.yx.hoststack.center.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yx.hoststack.center.common.req.channel.SendChannelReq;
import org.yx.hoststack.center.service.center.CenterService;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.SpringContextHolder;

@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/aaa")
    public R<?> test() {
        CenterService centerService = SpringContextHolder.getBean(CenterService.class);
        C2EMessage.EdgeBasicConfig.Builder basicBuilder = C2EMessage.EdgeBasicConfig.newBuilder()
                .setLocalShareStorageHttpSvc("2")
                .setShareStorageUser("2")
                .setShareStoragePwd("2")
                .setLocalLogSvcHttpSvc("2")
                .setNetLogSvcHttpsSvc("2")
                .setSpeedTestSvc("2")
                .setLocation("2");

        // 构建网络配置列表
//        List<C2EMessage.EdgeNetConfig> netConfigs =
//                req.getConfig().getNet().stream()
//                .map(IdcNetConfigReq::toEdgeNetConfig)
//                .collect(Collectors.toList());

        // 构建完整的配置同步请求
//        C2EMessage.C2E_EdgeConfigSyncReq configSyncReq = C2EMessage.C2E_EdgeConfigSyncReq.newBuilder()
//                .setBasic(basicBuilder.build())
//                .addAllNet(netConfigs)
//                .build();
        SendChannelReq sendChannelReq = new SendChannelReq();
        sendChannelReq.setServiceId("785223fd9aa01e5b28968401d9891abe");
        sendChannelReq.setHostId(null);
        sendChannelReq.setMsg(basicBuilder.build().toByteArray());

//        return centerService.sendMsgToLocalOrRemoteChannel(sendChannelReq);
        return centerService.fetchChannelFromRemote(sendChannelReq);
    }
}
