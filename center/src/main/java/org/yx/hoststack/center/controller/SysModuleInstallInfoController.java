package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.SysModuleInstallInfo;
import org.yx.hoststack.center.service.SysModuleInstallInfoService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * 系统模块安装信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/sysModuleInstallInfo")
@RequiredArgsConstructor
public class SysModuleInstallInfoController {

    private final SysModuleInstallInfoService sysModuleInstallInfoService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<SysModuleInstallInfo>> findPage(@RequestBody SysModuleInstallInfo params) {
        Page<SysModuleInstallInfo> result = sysModuleInstallInfoService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<SysModuleInstallInfo>> findList(@RequestBody SysModuleInstallInfo params) {
        List<SysModuleInstallInfo> result = sysModuleInstallInfoService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SysModuleInstallInfo> findById(@PathVariable("id") Long id) {
        SysModuleInstallInfo sysModuleInstallInfo = sysModuleInstallInfoService.findById(id);
        return R.ok(sysModuleInstallInfo);
    }

    /**
     * 新增
     *
     * @param sysModuleInstallInfo
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody SysModuleInstallInfo sysModuleInstallInfo) {
        boolean result = sysModuleInstallInfoService.insert(sysModuleInstallInfo);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param sysModuleInstallInfo
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody SysModuleInstallInfo sysModuleInstallInfo) {
        boolean result = sysModuleInstallInfoService.update(sysModuleInstallInfo);
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
        int result = sysModuleInstallInfoService.delete(id);
        return R.ok(result);
    }

}