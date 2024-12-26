package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.SysModule;
import org.yx.hoststack.center.service.SysModuleService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/sysModule")
@RequiredArgsConstructor
public class SysModuleController {

    private final SysModuleService sysModuleService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<SysModule>> findPage(@RequestBody SysModule params) {
        Page<SysModule> result = sysModuleService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<SysModule>> findList(@RequestBody SysModule params) {
        List<SysModule> result = sysModuleService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SysModule> findById(@PathVariable("id") Long id) {
        SysModule sysModule = sysModuleService.findById(id);
        return R.ok(sysModule);
    }

    /**
     * 新增
     *
     * @param sysModule
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody SysModule sysModule) {
        boolean result = sysModuleService.insert(sysModule);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param sysModule
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody SysModule sysModule) {
        boolean result = sysModuleService.update(sysModule);
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
        int result = sysModuleService.delete(id);
        return R.ok(result);
    }

}