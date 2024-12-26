package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.TenantResourceGroup;
import org.yx.hoststack.center.service.TenantResourceGroupService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * 资源分组表
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/tenantResourceGroup")
@RequiredArgsConstructor
public class TenantResourceGroupController {

    private final TenantResourceGroupService tenantResourceGroupService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<TenantResourceGroup>> findPage(@RequestBody TenantResourceGroup params) {
        Page<TenantResourceGroup> result = tenantResourceGroupService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<TenantResourceGroup>> findList(@RequestBody TenantResourceGroup params) {
        List<TenantResourceGroup> result = tenantResourceGroupService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<TenantResourceGroup> findById(@PathVariable("id") Long id) {
        TenantResourceGroup tenantResourceGroup = tenantResourceGroupService.findById(id);
        return R.ok(tenantResourceGroup);
    }

    /**
     * 新增
     *
     * @param tenantResourceGroup
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody TenantResourceGroup tenantResourceGroup) {
        boolean result = tenantResourceGroupService.insert(tenantResourceGroup);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param tenantResourceGroup
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody TenantResourceGroup tenantResourceGroup) {
        boolean result = tenantResourceGroupService.update(tenantResourceGroup);
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
        int result = tenantResourceGroupService.delete(id);
        return R.ok(result);
    }

}