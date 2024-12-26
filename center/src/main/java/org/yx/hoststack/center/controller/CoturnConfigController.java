package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.common.constant.RequestMappingBase;
import org.yx.hoststack.center.common.mybatisplus.tool.support.Query;
import org.yx.hoststack.center.common.req.coturn.CoturnConfigReq;
import org.yx.hoststack.center.common.req.ossconfig.OssConfigReq;
import org.yx.hoststack.center.entity.CoturnConfig;
import org.yx.hoststack.center.entity.OssConfig;
import org.yx.hoststack.center.mapper.CoturnConfigMapper;
import org.yx.hoststack.center.service.CoturnConfigService;
import org.yx.hoststack.center.service.OssConfigService;
import org.yx.lib.utils.util.R;

import java.util.Arrays;
import java.util.Map;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping(RequestMappingBase.admin + RequestMappingBase.region_coturn)
@RequiredArgsConstructor
public class CoturnConfigController {

    private final CoturnConfigService coturnConfigService;
    private final OssConfigService ossConfigService;


    @GetMapping("/page")
    public R<Page<CoturnConfig>> findPage(Query query,
                                          @RequestParam(required = false) Map<String, Object> params) {
        return R.ok(coturnConfigService.findPage(query, new ModelMapper().map(params, CoturnConfig.class)));
    }

    @GetMapping("/info")
    public R<CoturnConfig> info(@RequestParam String region) {
        return R.ok(coturnConfigService.getOneOpt(new LambdaQueryWrapper<CoturnConfig>().eq(CoturnConfig::getRegion, region)).orElse(new CoturnConfig()));
    }

    @PostMapping("config")
    public R<Boolean> insert(@RequestBody @Valid CoturnConfigReq coturnConfigReq) {
        Boolean result = coturnConfigService.save(new ModelMapper().map(coturnConfigReq, CoturnConfig.class));
        result = ossConfigService.save(new ModelMapper().map(coturnConfigReq, OssConfig.class));
        return R.ok(result);
    }

    @PostMapping("update")
    public R<Boolean> update(@RequestBody CoturnConfig coturnConfig) {
        boolean result = coturnConfigService.update(coturnConfig);
        return R.ok(result);
    }

    @GetMapping("/delete")
    public R<Integer> delete(@RequestParam Long id) {
        int result = coturnConfigService.delete(id);
        return R.ok(result);
    }
}