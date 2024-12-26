package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.lib.utils.util.R;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.AccountInfo;
import org.yx.hoststack.center.service.AccountInfoService;

import java.util.List;

/**
 * 账户信息表
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/accountInfo")
@RequiredArgsConstructor
public class AccountInfoController {

    private final AccountInfoService accountInfoService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<AccountInfo>> findPage(@RequestBody AccountInfo params) {
        Page<AccountInfo> result = accountInfoService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<AccountInfo>> findList(@RequestBody AccountInfo params) {
        List<AccountInfo> result = accountInfoService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<AccountInfo> findById(@PathVariable("id") Long id) {
        AccountInfo accountInfo = accountInfoService.findById(id);
        return R.ok(accountInfo);
    }

    /**
     * 新增
     *
     * @param accountInfo
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody AccountInfo accountInfo) {
        boolean result = accountInfoService.insert(accountInfo);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param accountInfo
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody AccountInfo accountInfo) {
        boolean result = accountInfoService.update(accountInfo);
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
        int result = accountInfoService.delete(id);
        return R.ok(result);
    }

}