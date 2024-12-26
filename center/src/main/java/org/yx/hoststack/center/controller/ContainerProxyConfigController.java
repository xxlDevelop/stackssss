package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.lib.utils.util.R;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.ContainerProxyConfig;
import org.yx.hoststack.center.service.ContainerProxyConfigService;

import java.util.List;

/**
 * 容器代理信息配置
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/containerProxyConfig")
@RequiredArgsConstructor
public class ContainerProxyConfigController {

    private final ContainerProxyConfigService containerProxyConfigService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<ContainerProxyConfig>> findPage(@RequestBody ContainerProxyConfig params) {
        Page<ContainerProxyConfig> result = containerProxyConfigService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<ContainerProxyConfig>> findList(@RequestBody ContainerProxyConfig params) {
        List<ContainerProxyConfig> result = containerProxyConfigService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<ContainerProxyConfig> findById(@PathVariable("id") Long id) {
        ContainerProxyConfig containerProxyConfig = containerProxyConfigService.findById(id);
        return R.ok(containerProxyConfig);
    }

    /**
     * 新增
     *
     * @param containerProxyConfig
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody ContainerProxyConfig containerProxyConfig) {
        boolean result = containerProxyConfigService.insert(containerProxyConfig);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param containerProxyConfig
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody ContainerProxyConfig containerProxyConfig) {
        boolean result = containerProxyConfigService.update(containerProxyConfig);
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
        int result = containerProxyConfigService.delete(id);
        return R.ok(result);
    }

}