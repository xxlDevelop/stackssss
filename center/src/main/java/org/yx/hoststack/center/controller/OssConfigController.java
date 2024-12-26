package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.common.req.ossconfig.OssConfigReq;
import org.yx.hoststack.center.common.req.ossconfig.RegionSyncReq;
import org.yx.hoststack.center.entity.CoturnConfig;
import org.yx.hoststack.center.entity.OssConfig;
import org.yx.hoststack.center.service.OssConfigService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * 机房存储信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/region")
@RequiredArgsConstructor
public class OssConfigController {

    private final OssConfigService ossConfigService;

    @GetMapping("/oss/info")
    public R<OssConfig> info(@RequestParam String region) {
        return R.ok(ossConfigService.getOneOpt(new LambdaQueryWrapper<OssConfig>().eq(OssConfig::getRegion, region)).orElse(new OssConfig()));
    }

    @PostMapping("/oss/config")
    public R<Boolean> insert(@RequestBody @Valid OssConfigReq ossConfigReq) {
        boolean result = ossConfigService.insert(new ModelMapper().map(ossConfigReq, OssConfig.class));
        return R.ok(result);
    }


    @PostMapping("/sync")
    public R<Boolean> sync(@RequestBody @Valid List<RegionSyncReq> syncReqs) {
        return R.ok(true);
    }

}