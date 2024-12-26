package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.PackageFilesInfo;
import org.yx.hoststack.center.service.PackageFilesInfoService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * 文件提取打包信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/packageFilesInfo")
@RequiredArgsConstructor
public class PackageFilesInfoController {

    private final PackageFilesInfoService packageFilesInfoService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<PackageFilesInfo>> findPage(@RequestBody PackageFilesInfo params) {
        Page<PackageFilesInfo> result = packageFilesInfoService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<PackageFilesInfo>> findList(@RequestBody PackageFilesInfo params) {
        List<PackageFilesInfo> result = packageFilesInfoService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<PackageFilesInfo> findById(@PathVariable("id") Long id) {
        PackageFilesInfo packageFilesInfo = packageFilesInfoService.findById(id);
        return R.ok(packageFilesInfo);
    }

    /**
     * 新增
     *
     * @param packageFilesInfo
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody PackageFilesInfo packageFilesInfo) {
        boolean result = packageFilesInfoService.insert(packageFilesInfo);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param packageFilesInfo
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody PackageFilesInfo packageFilesInfo) {
        boolean result = packageFilesInfoService.update(packageFilesInfo);
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
        int result = packageFilesInfoService.delete(id);
        return R.ok(result);
    }

}