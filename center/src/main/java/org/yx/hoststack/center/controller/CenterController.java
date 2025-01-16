package org.yx.hoststack.center.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yx.hoststack.center.common.constant.RequestMappingBase;
import org.yx.hoststack.center.common.req.channel.SendChannelReq;
import org.yx.hoststack.center.service.CenterService;
import org.yx.lib.utils.util.R;

@RestController
@RequestMapping(RequestMappingBase.admin + RequestMappingBase.center)
@RequiredArgsConstructor
public class CenterController {

    private CenterService centerService;

    @GetMapping("/channel")
    public R<?> sendMsgToChannel(@Validated SendChannelReq request) {
        return centerService.sendMsgToChannel(request);
    }
}