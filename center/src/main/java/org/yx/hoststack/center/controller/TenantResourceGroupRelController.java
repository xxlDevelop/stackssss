package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.TenantResourceGroupRel;
import org.yx.hoststack.center.service.TenantResourceGroupRelService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * 资源分组与资源绑定关系表
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/tenantResourceGroupRel")
@RequiredArgsConstructor
public class TenantResourceGroupRelController {

    private final TenantResourceGroupRelService tenantResourceGroupRelService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<TenantResourceGroupRel>> findPage(@RequestBody TenantResourceGroupRel params) {
        Page<TenantResourceGroupRel> result = tenantResourceGroupRelService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<TenantResourceGroupRel>> findList(@RequestBody TenantResourceGroupRel params) {
        List<TenantResourceGroupRel> result = tenantResourceGroupRelService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<TenantResourceGroupRel> findById(@PathVariable("id") Long id) {
        TenantResourceGroupRel tenantResourceGroupRel = tenantResourceGroupRelService.findById(id);
        return R.ok(tenantResourceGroupRel);
    }

    /**
     * 新增
     *
     * @param tenantResourceGroupRel
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody TenantResourceGroupRel tenantResourceGroupRel) {
        boolean result = tenantResourceGroupRelService.insert(tenantResourceGroupRel);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param tenantResourceGroupRel
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody TenantResourceGroupRel tenantResourceGroupRel) {
        boolean result = tenantResourceGroupRelService.update(tenantResourceGroupRel);
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
        int result = tenantResourceGroupRelService.delete(id);
        return R.ok(result);
    }

}