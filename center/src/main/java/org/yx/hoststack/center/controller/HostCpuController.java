package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.HostCpu;
import org.yx.hoststack.center.service.HostCpuService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * AGENT CPU信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/hostCpu")
@RequiredArgsConstructor
public class HostCpuController {

    private final HostCpuService hostCpuService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<HostCpu>> findPage(@RequestBody HostCpu params) {
        Page<HostCpu> result = hostCpuService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<HostCpu>> findList(@RequestBody HostCpu params) {
        List<HostCpu> result = hostCpuService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<HostCpu> findById(@PathVariable("id") Long id) {
        HostCpu hostCpu = hostCpuService.findById(id);
        return R.ok(hostCpu);
    }

    /**
     * 新增
     *
     * @param hostCpu
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody HostCpu hostCpu) {
        boolean result = hostCpuService.insert(hostCpu);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param hostCpu
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody HostCpu hostCpu) {
        boolean result = hostCpuService.update(hostCpu);
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
        int result = hostCpuService.delete(id);
        return R.ok(result);
    }

}