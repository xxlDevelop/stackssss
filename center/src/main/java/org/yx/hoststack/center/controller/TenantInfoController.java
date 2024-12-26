package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.TenantInfo;
import org.yx.hoststack.center.service.TenantInfoService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * 租户信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/tenantInfo")
@RequiredArgsConstructor
public class TenantInfoController {

    private final TenantInfoService tenantInfoService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<TenantInfo>> findPage(@RequestBody TenantInfo params) {
        Page<TenantInfo> result = tenantInfoService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<TenantInfo>> findList(@RequestBody TenantInfo params) {
        List<TenantInfo> result = tenantInfoService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<TenantInfo> findById(@PathVariable("id") Long id) {
        TenantInfo tenantInfo = tenantInfoService.findById(id);
        return R.ok(tenantInfo);
    }

    /**
     * 新增
     *
     * @param tenantInfo
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody TenantInfo tenantInfo) {
        boolean result = tenantInfoService.insert(tenantInfo);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param tenantInfo
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody TenantInfo tenantInfo) {
        boolean result = tenantInfoService.update(tenantInfo);
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
        int result = tenantInfoService.delete(id);
        return R.ok(result);
    }

}