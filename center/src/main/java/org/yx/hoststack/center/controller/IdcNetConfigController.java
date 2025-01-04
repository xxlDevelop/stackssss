package org.yx.hoststack.center.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yx.hoststack.center.common.constant.RequestMappingBase;
import org.yx.hoststack.center.common.req.idc.net.IdcNetConfigListReq;
import org.yx.hoststack.center.common.req.idc.net.IdcNetConfigReq;
import org.yx.hoststack.center.service.IdcNetConfigService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * 机房网络配置
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping(RequestMappingBase.admin + RequestMappingBase.idc_net)
@RequiredArgsConstructor
public class IdcNetConfigController {

    private final IdcNetConfigService idcNetConfigService;


    @PostMapping("/config")
    public R<?> config(@RequestBody
                       @Valid
                       @NotEmpty(message = "The idcNetConfigList must not be empty.") List<@Valid IdcNetConfigReq> idcNetConfigReqList) {
        return idcNetConfigService.saveConfig(idcNetConfigReqList);
    }


    /**
     * List IDC network configurations
     *
     * @param req query parameters
     * @return R<?> with list of network configurations
     */
    @PostMapping("/list")
    public R<?> list(@RequestBody @Valid IdcNetConfigListReq req) {
        return idcNetConfigService.list(req);
    }

}