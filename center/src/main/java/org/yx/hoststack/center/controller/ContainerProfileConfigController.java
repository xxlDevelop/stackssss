package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.lib.utils.util.R;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.ContainerProfileConfig;
import org.yx.hoststack.center.service.ContainerProfileConfigService;

import java.util.List;

/**
 * 容器网络配置
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/containerProfileConfig")
@RequiredArgsConstructor
public class ContainerProfileConfigController {

    private final ContainerProfileConfigService containerProfileConfigService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<ContainerProfileConfig>> findPage(@RequestBody ContainerProfileConfig params) {
        Page<ContainerProfileConfig> result = containerProfileConfigService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<ContainerProfileConfig>> findList(@RequestBody ContainerProfileConfig params) {
        List<ContainerProfileConfig> result = containerProfileConfigService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<ContainerProfileConfig> findById(@PathVariable("id") Long id) {
        ContainerProfileConfig containerProfileConfig = containerProfileConfigService.findById(id);
        return R.ok(containerProfileConfig);
    }

    /**
     * 新增
     *
     * @param containerProfileConfig
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody ContainerProfileConfig containerProfileConfig) {
        boolean result = containerProfileConfigService.insert(containerProfileConfig);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param containerProfileConfig
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody ContainerProfileConfig containerProfileConfig) {
        boolean result = containerProfileConfigService.update(containerProfileConfig);
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
        int result = containerProfileConfigService.delete(id);
        return R.ok(result);
    }

}