package org.yx.hoststack.center.controller.center;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yx.hoststack.center.common.constant.RequestMappingBase;
import org.yx.hoststack.center.common.dto.ServiceDetailDTO;
import org.yx.hoststack.center.common.req.channel.SendChannelBasic;
import org.yx.hoststack.center.common.req.channel.SendChannelReq;
import org.yx.hoststack.center.service.center.CenterService;
import org.yx.lib.utils.util.R;

@RestController
@RequestMapping(RequestMappingBase.admin + RequestMappingBase.center)
public class CenterController {

    CenterService centerService;

    public CenterController(CenterService centerService) {
        this.centerService = centerService;
    }

    @PostMapping("/channel")
    public R<?> sendMsgToChannel(@RequestBody @Validated SendChannelReq request) {
        return centerService.sendMsgToLocalChannel(request);
    }

    /**
     * 远端查找节点
     *
     * @author yijian
     * @date 2025/1/22 12:32
     */
    @PostMapping("/find/node")
    public ServiceDetailDTO sendMsgToChannel(@RequestBody @Validated SendChannelBasic request) {
        return centerService.findNode(request);
    }
}