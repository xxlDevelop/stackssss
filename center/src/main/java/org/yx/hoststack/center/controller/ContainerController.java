package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.lib.utils.util.R;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.Container;
import org.yx.hoststack.center.service.ContainerService;

import java.util.List;

/**
 * 容器信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/container")
@RequiredArgsConstructor
public class ContainerController {

    private final ContainerService containerService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<Container>> findPage(@RequestBody Container params) {
        Page<Container> result = containerService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<Container>> findList(@RequestBody Container params) {
        List<Container> result = containerService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Container> findById(@PathVariable("id") Long id) {
        Container container = containerService.findById(id);
        return R.ok(container);
    }

    /**
     * 新增
     *
     * @param container
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody Container container) {
        boolean result = containerService.insert(container);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param container
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody Container container) {
        boolean result = containerService.update(container);
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
        int result = containerService.delete(id);
        return R.ok(result);
    }

}