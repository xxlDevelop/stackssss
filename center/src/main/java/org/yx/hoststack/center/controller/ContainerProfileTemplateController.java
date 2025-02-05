package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.lib.utils.util.R;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.ContainerProfileTemplate;
import org.yx.hoststack.center.service.ContainerProfileTemplateService;

import java.util.List;

/**
 * 容器模板
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/containerProfileTemplate")
@RequiredArgsConstructor
public class ContainerProfileTemplateController {

    private final ContainerProfileTemplateService containerProfileTemplateService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<ContainerProfileTemplate>> findPage(@RequestBody ContainerProfileTemplate params) {
        Page<ContainerProfileTemplate> result = containerProfileTemplateService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<ContainerProfileTemplate>> findList(@RequestBody ContainerProfileTemplate params) {
        List<ContainerProfileTemplate> result = containerProfileTemplateService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<ContainerProfileTemplate> findById(@PathVariable("id") Long id) {
        ContainerProfileTemplate containerProfileTemplate = containerProfileTemplateService.findById(id);
        return R.ok(containerProfileTemplate);
    }

    /**
     * 新增
     *
     * @param containerProfileTemplate
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody ContainerProfileTemplate containerProfileTemplate) {
        boolean result = containerProfileTemplateService.insert(containerProfileTemplate);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param containerProfileTemplate
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody ContainerProfileTemplate containerProfileTemplate) {
        boolean result = containerProfileTemplateService.update(containerProfileTemplate);
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
        int result = containerProfileTemplateService.delete(id);
        return R.ok(result);
    }

}