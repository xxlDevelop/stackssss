package org.yx.hoststack.center.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.yx.hoststack.center.common.req.channel.SendChannelBasic;
import org.yx.hoststack.center.common.req.channel.SendChannelReq;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.StringUtil;

@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/aaa")
    public R<?> test() {
        int a = 0;
        int b = 0;
        int c = a / b;
        return R.ok();
    }

    @GetMapping("/error")
    public R<?> error() {
        SendChannelReq sendChannelReq = SendChannelReq.builder().msg(new byte[]{1, 2, 3}).serviceId("11").build();

        String remoteUrl = buildRemoteUrl(sendChannelReq, "sdfsdf");
        throw new RuntimeException("error");
    }

    private String buildRemoteUrl(SendChannelBasic sendChannelBasic, String url) {
        String relaySid = StringUtil.isBlank(sendChannelBasic.getHostId()) ? sendChannelBasic.getServiceId() : sendChannelBasic.getHostId();
        String remoteServer = "1212";
        if (remoteServer == null) {
            throw new RuntimeException("BuildRemoteUrl Remote server not found");
        }
        return UriComponentsBuilder.fromUriString("http://" + remoteServer)
                .path(url)
                .build()
                .toUriString();
    }
}
