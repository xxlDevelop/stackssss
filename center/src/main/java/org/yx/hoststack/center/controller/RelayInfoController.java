package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.RelayInfo;
import org.yx.hoststack.center.service.RelayInfoService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * 中继转发节点信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/relayInfo")
@RequiredArgsConstructor
public class RelayInfoController {

    private final RelayInfoService relayInfoService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<RelayInfo>> findPage(@RequestBody RelayInfo params) {
        Page<RelayInfo> result = relayInfoService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<RelayInfo>> findList(@RequestBody RelayInfo params) {
        List<RelayInfo> result = relayInfoService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<RelayInfo> findById(@PathVariable("id") Long id) {
        RelayInfo relayInfo = relayInfoService.findById(id);
        return R.ok(relayInfo);
    }

    /**
     * 新增
     *
     * @param relayInfo
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody RelayInfo relayInfo) {
        boolean result = relayInfoService.insert(relayInfo);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param relayInfo
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody RelayInfo relayInfo) {
        boolean result = relayInfoService.update(relayInfo);
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
        int result = relayInfoService.delete(id);
        return R.ok(result);
    }

}