package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.Volume;
import org.yx.hoststack.center.service.VolumeService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * 存储卷
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/volume")
@RequiredArgsConstructor
public class VolumeController {

    private final VolumeService volumeService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<Volume>> findPage(@RequestBody Volume params) {
        Page<Volume> result = volumeService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<Volume>> findList(@RequestBody Volume params) {
        List<Volume> result = volumeService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Volume> findById(@PathVariable("id") Long id) {
        Volume volume = volumeService.findById(id);
        return R.ok(volume);
    }

    /**
     * 新增
     *
     * @param volume
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody Volume volume) {
        boolean result = volumeService.insert(volume);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param volume
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody Volume volume) {
        boolean result = volumeService.update(volume);
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
        int result = volumeService.delete(id);
        return R.ok(result);
    }

}