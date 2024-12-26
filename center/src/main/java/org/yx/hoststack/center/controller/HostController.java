package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.Host;
import org.yx.hoststack.center.service.HostService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * AGENT信息表,存储HOSTAGENT和CONTAINERAGENT信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/host")
@RequiredArgsConstructor
public class HostController {

    private final HostService hostService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<Host>> findPage(@RequestBody Host params) {
        Page<Host> result = hostService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<Host>> findList(@RequestBody Host params) {
        List<Host> result = hostService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Host> findById(@PathVariable("id") Long id) {
        Host host = hostService.findById(id);
        return R.ok(host);
    }

    /**
     * 新增
     *
     * @param host
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody Host host) {
        boolean result = hostService.insert(host);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param host
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody Host host) {
        boolean result = hostService.update(host);
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
        int result = hostService.delete(id);
        return R.ok(result);
    }

}