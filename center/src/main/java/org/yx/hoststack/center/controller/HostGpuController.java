package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.HostGpu;
import org.yx.hoststack.center.service.HostGpuService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * AGENT GPU信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/hostGpu")
@RequiredArgsConstructor
public class HostGpuController {

    private final HostGpuService hostGpuService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<HostGpu>> findPage(@RequestBody HostGpu params) {
        Page<HostGpu> result = hostGpuService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<HostGpu>> findList(@RequestBody HostGpu params) {
        List<HostGpu> result = hostGpuService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<HostGpu> findById(@PathVariable("id") Long id) {
        HostGpu hostGpu = hostGpuService.findById(id);
        return R.ok(hostGpu);
    }

    /**
     * 新增
     *
     * @param hostGpu
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody HostGpu hostGpu) {
        boolean result = hostGpuService.insert(hostGpu);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param hostGpu
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody HostGpu hostGpu) {
        boolean result = hostGpuService.update(hostGpu);
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
        int result = hostGpuService.delete(id);
        return R.ok(result);
    }

}