package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.common.req.container.ContainerProfileTemplatePageReqDTO;
import org.yx.hoststack.center.common.resp.comtainer.ContainerProfileTemplateSimpleVO;
import org.yx.hoststack.center.common.resp.comtainer.wrappers.ContainerProfileTemplateSimpleVOWrapper;
import org.yx.hoststack.center.entity.ContainerProfileTemplate;
import org.yx.hoststack.center.service.ContainerProfileTemplateService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * 容器模板
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/v1/container/profile")
@RequiredArgsConstructor
public class ContainerProfileTemplateController {

    private final ContainerProfileTemplateService containerProfileTemplateService;

    /**
     * query container template page list
     *
     * @param params request
     * @return result
     */
    @PostMapping("/list")
    public R<IPage<ContainerProfileTemplateSimpleVO>> findPage(@Valid @Validated @RequestBody ContainerProfileTemplatePageReqDTO params) {
        return R.ok(ContainerProfileTemplateSimpleVOWrapper.build().pageVO(containerProfileTemplateService.findPage(params)));
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