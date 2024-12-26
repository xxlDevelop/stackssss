package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.lib.utils.util.R;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.ContainerDeployTaskDetail;
import org.yx.hoststack.center.service.ContainerDeployTaskDetailService;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/containerDeployTaskDetail")
@RequiredArgsConstructor
public class ContainerDeployTaskDetailController {

    private final ContainerDeployTaskDetailService containerDeployTaskDetailService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<ContainerDeployTaskDetail>> findPage(@RequestBody ContainerDeployTaskDetail params) {
        Page<ContainerDeployTaskDetail> result = containerDeployTaskDetailService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<ContainerDeployTaskDetail>> findList(@RequestBody ContainerDeployTaskDetail params) {
        List<ContainerDeployTaskDetail> result = containerDeployTaskDetailService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<ContainerDeployTaskDetail> findById(@PathVariable("id") Long id) {
        ContainerDeployTaskDetail containerDeployTaskDetail = containerDeployTaskDetailService.findById(id);
        return R.ok(containerDeployTaskDetail);
    }

    /**
     * 新增
     *
     * @param containerDeployTaskDetail
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody ContainerDeployTaskDetail containerDeployTaskDetail) {
        boolean result = containerDeployTaskDetailService.insert(containerDeployTaskDetail);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param containerDeployTaskDetail
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody ContainerDeployTaskDetail containerDeployTaskDetail) {
        boolean result = containerDeployTaskDetailService.update(containerDeployTaskDetail);
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
        int result = containerDeployTaskDetailService.delete(id);
        return R.ok(result);
    }

}