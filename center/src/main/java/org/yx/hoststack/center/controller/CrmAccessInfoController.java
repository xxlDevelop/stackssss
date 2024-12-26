package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.lib.utils.util.R;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.CrmAccessInfo;
import org.yx.hoststack.center.service.CrmAccessInfoService;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/crmAccessInfo")
@RequiredArgsConstructor
public class CrmAccessInfoController {

    private final CrmAccessInfoService crmAccessInfoService;

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
     * 新增
     *
     * @param crmAccessInfo
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody CrmAccessInfo crmAccessInfo) {
        boolean result = crmAccessInfoService.insert(crmAccessInfo);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param crmAccessInfo
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody CrmAccessInfo crmAccessInfo) {
        boolean result = crmAccessInfoService.update(crmAccessInfo);
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
        int result = crmAccessInfoService.delete(id);
        return R.ok(result);
    }

}