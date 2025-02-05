package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.common.constant.RequestMappingBase;
import org.yx.hoststack.center.common.req.idc.net.IdcNetConfigReq;
import org.yx.hoststack.center.entity.IdcNetConfig;
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
@RequestMapping(RequestMappingBase.admin + RequestMappingBase.idc)
@RequiredArgsConstructor
public class IdcNetConfigController {

    private final IdcNetConfigService idcNetConfigService;


    @PostMapping("/config")
    public R<Boolean> config(@RequestBody
                                 @Valid
                                 @NotEmpty(message = "The idcNetConfigList must not be empty.") List<@Valid IdcNetConfigReq> idcNetConfigReqList) {
        idcNetConfigService.saveConfig(idcNetConfigReqList);
        return R.ok(true);
    }

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<IdcNetConfig>> findPage(@RequestBody IdcNetConfig params) {
        Page<IdcNetConfig> result = idcNetConfigService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<IdcNetConfig>> findList(@RequestBody IdcNetConfig params) {
        List<IdcNetConfig> result = idcNetConfigService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<IdcNetConfig> findById(@PathVariable("id") Long id) {
        IdcNetConfig idcNetConfig = idcNetConfigService.findById(id);
        return R.ok(idcNetConfig);
    }

    /**
     * 新增
     *
     * @param idcNetConfig
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody IdcNetConfig idcNetConfig) {
        boolean result = idcNetConfigService.insert(idcNetConfig);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param idcNetConfig
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody IdcNetConfig idcNetConfig) {
        boolean result = idcNetConfigService.update(idcNetConfig);
        return R.ok(result);
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public R<Integer> delete(@PathVariable("id") Long id) {
        int result = idcNetConfigService.delete(id);
        return R.ok(result);
    }

}