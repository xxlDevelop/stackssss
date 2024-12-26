package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.VolumeMountRel;
import org.yx.hoststack.center.service.VolumeMountRelService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * 存储卷挂载关系
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/volumeMountRel")
@RequiredArgsConstructor
public class VolumeMountRelController {

    private final VolumeMountRelService volumeMountRelService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<VolumeMountRel>> findPage(@RequestBody VolumeMountRel params) {
        Page<VolumeMountRel> result = volumeMountRelService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<VolumeMountRel>> findList(@RequestBody VolumeMountRel params) {
        List<VolumeMountRel> result = volumeMountRelService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<VolumeMountRel> findById(@PathVariable("id") Long id) {
        VolumeMountRel volumeMountRel = volumeMountRelService.findById(id);
        return R.ok(volumeMountRel);
    }

    /**
     * 新增
     *
     * @param volumeMountRel
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody VolumeMountRel volumeMountRel) {
        boolean result = volumeMountRelService.insert(volumeMountRel);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param volumeMountRel
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody VolumeMountRel volumeMountRel) {
        boolean result = volumeMountRelService.update(volumeMountRel);
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
        int result = volumeMountRelService.delete(id);
        return R.ok(result);
    }

}