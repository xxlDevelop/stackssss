package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.lib.utils.util.R;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.ContainerDeployTask;
import org.yx.hoststack.center.service.ContainerDeployTaskService;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/containerDeployTask")
@RequiredArgsConstructor
public class ContainerDeployTaskController {

    private final ContainerDeployTaskService containerDeployTaskService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<ContainerDeployTask>> findPage(@RequestBody ContainerDeployTask params) {
        Page<ContainerDeployTask> result = containerDeployTaskService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<ContainerDeployTask>> findList(@RequestBody ContainerDeployTask params) {
        List<ContainerDeployTask> result = containerDeployTaskService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<ContainerDeployTask> findById(@PathVariable("id") Long id) {
        ContainerDeployTask containerDeployTask = containerDeployTaskService.findById(id);
        return R.ok(containerDeployTask);
    }

    /**
     * 新增
     *
     * @param containerDeployTask
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody ContainerDeployTask containerDeployTask) {
        boolean result = containerDeployTaskService.insert(containerDeployTask);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param containerDeployTask
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody ContainerDeployTask containerDeployTask) {
        boolean result = containerDeployTaskService.update(containerDeployTask);
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
        int result = containerDeployTaskService.delete(id);
        return R.ok(result);
    }

}