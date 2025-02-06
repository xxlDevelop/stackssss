package org.yx.hoststack.center.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yx.hoststack.center.common.constant.RequestMappingBase;
import org.yx.hoststack.center.common.req.idc.IdcCreateReq;
import org.yx.hoststack.center.common.req.idc.IdcListReq;
import org.yx.hoststack.center.common.req.idc.IdcUpdateReq;
import org.yx.hoststack.center.common.req.idc.config.IdcConfigSyncReq;
import org.yx.hoststack.center.common.resp.PageResp;
import org.yx.hoststack.center.common.resp.idc.CreateIdcInfoResp;
import org.yx.hoststack.center.common.resp.idc.IdcListResp;
import org.yx.hoststack.center.service.IdcInfoService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * 机房信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping(RequestMappingBase.admin + RequestMappingBase.idc)
@RequiredArgsConstructor
public class IdcInfoController {

    private final IdcInfoService idcInfoService;

    /**
     * list
     *
     * @param idcListReq
     * @return
     */
    @PostMapping("/list")
    public R<PageResp<IdcListResp>> list(@RequestBody @Validated IdcListReq idcListReq) {
        return idcInfoService.list(idcListReq);
    }

    /**
     * create
     *
     * @param idcCreateReqReq
     * @return
     */
    @PostMapping("/create")
    public R<CreateIdcInfoResp> create(@RequestBody @Validated IdcCreateReq idcCreateReqReq) {
        return R.ok(idcInfoService.create(idcCreateReqReq));
    }

    /**
     * update
     *
     * @param idcUpdateReqReq
     * @return
     */
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody @Validated IdcUpdateReq idcUpdateReqReq) {
        boolean result = idcInfoService.update(idcUpdateReqReq);
        return R.ok(result);
    }

    @PostMapping("/config/sync")
    public R<?> syncConfig(@RequestBody @Validated List<@Valid IdcConfigSyncReq> syncReqList) {
        return idcInfoService.syncConfig(syncReqList);
    }


}