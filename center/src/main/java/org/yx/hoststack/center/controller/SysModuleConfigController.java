package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.SysModuleConfig;
import org.yx.hoststack.center.service.SysModuleConfigService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * 模块安装配置信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/sysModuleConfig")
@RequiredArgsConstructor
public class SysModuleConfigController {

    private final SysModuleConfigService sysModuleConfigService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<SysModuleConfig>> findPage(@RequestBody SysModuleConfig params) {
        Page<SysModuleConfig> result = sysModuleConfigService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<SysModuleConfig>> findList(@RequestBody SysModuleConfig params) {
        List<SysModuleConfig> result = sysModuleConfigService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SysModuleConfig> findById(@PathVariable("id") Long id) {
        SysModuleConfig sysModuleConfig = sysModuleConfigService.findById(id);
        return R.ok(sysModuleConfig);
    }

    /**
     * 新增
     *
     * @param sysModuleConfig
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody SysModuleConfig sysModuleConfig) {
        boolean result = sysModuleConfigService.insert(sysModuleConfig);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param sysModuleConfig
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody SysModuleConfig sysModuleConfig) {
        boolean result = sysModuleConfigService.update(sysModuleConfig);
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
        int result = sysModuleConfigService.delete(id);
        return R.ok(result);
    }

}