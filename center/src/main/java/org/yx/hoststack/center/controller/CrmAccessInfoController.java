package org.yx.hoststack.center.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Base64Util;
import org.springframework.util.CollectionUtils;
import org.yx.hoststack.center.common.constant.RequestMappingBase;
import org.yx.hoststack.center.common.dto.XUserDTO;
import org.yx.hoststack.center.common.req.host.HostRegisterReq;
import org.yx.hoststack.center.common.req.host.HostReq;
import org.yx.hoststack.center.entity.Container;
import org.yx.hoststack.center.entity.Host;
import org.yx.hoststack.center.service.ContainerService;
import org.yx.hoststack.center.service.HostService;
import org.yx.lib.utils.util.R;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.CrmAccessInfo;
import org.yx.hoststack.center.service.CrmAccessInfoService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping(RequestMappingBase.admin + RequestMappingBase.crm_access)
@RequiredArgsConstructor
public class CrmAccessInfoController {

    private final CrmAccessInfoService crmAccessInfoService;
    private final HostService hostService;
    private final ContainerService containerService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<CrmAccessInfo>> findPage(@RequestBody CrmAccessInfo params) {
        Page<CrmAccessInfo> result = crmAccessInfoService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<CrmAccessInfo>> findList(@RequestBody CrmAccessInfo params) {
        List<CrmAccessInfo> result = crmAccessInfoService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<CrmAccessInfo> findById(@PathVariable("id") Long id) {
        CrmAccessInfo crmAccessInfo = crmAccessInfoService.findById(id);
        return R.ok(crmAccessInfo);
    }

    /**
     * 设置CRM访问权限
     *
     * @param crmAccessInfo
     * @return
     */
    @PostMapping("set")
    public R<Boolean> crmAccessSet(@RequestBody CrmAccessInfo crmAccessInfo,
                             @RequestHeader("x-user") String user) {
        String encode = Base64Util.encode(user);
        XUserDTO xUserDTO = JSONObject.parseObject(encode, XUserDTO.class);
        crmAccessInfo.setTenantId(xUserDTO.getTid());

        boolean result = crmAccessInfoService.insert(crmAccessInfo);
        if(result){
            hostService.update(new LambdaUpdateWrapper<Host>().set(Host::getAk,crmAccessInfo.getAk()).set(Host::getSk,crmAccessInfo.getSk()).eq(Host::getProviderTenantId,crmAccessInfo.getTenantId()));
            containerService.update(new LambdaUpdateWrapper<Container>().set(Container::getAk,crmAccessInfo.getAk()).set(Container::getSk,crmAccessInfo.getSk()).eq(Container::getProviderTenantId,crmAccessInfo.getTenantId()));
        }

        return R.ok(result);
    }

//    /**
//     * Host注入CRM
//     *
//     * @return
//     */
//    @PostMapping("register")
//    public R<Boolean> register(@RequestBody @Valid HostRegisterReq registerReq) {
//        List<String> cIds = Stream.of(registerReq.getHostIds().split(",")).collect(Collectors.toList());
//        hostService.list(new LambdaQueryWrapper<Host>().eq(registerReq.getRegion()!=null,Host::getRegion,registerReq.getRegion()).in(!CollectionUtils.isEmpty(cIds),Host::getHostId,cIds))
//
//        hostService.update(new LambdaUpdateWrapper<Host>().set(Host::getAk,crmAccessInfo.getAk()).set(Host::getSk,crmAccessInfo.getSk()).eq(Host::getProviderTenantId,crmAccessInfo.getTenantId()));
//
//
//
//        return R.ok(result);
//    }


}