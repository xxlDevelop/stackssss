package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.JobInfo;
import org.yx.hoststack.center.service.JobInfoService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * 任务信息表
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/jobInfo")
@RequiredArgsConstructor
public class JobInfoController {

    private final JobInfoService jobInfoService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<JobInfo>> findPage(@RequestBody JobInfo params) {
        Page<JobInfo> result = jobInfoService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<JobInfo>> findList(@RequestBody JobInfo params) {
        List<JobInfo> result = jobInfoService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<JobInfo> findById(@PathVariable("id") Long id) {
        JobInfo jobInfo = jobInfoService.findById(id);
        return R.ok(jobInfo);
    }

    /**
     * 新增
     *
     * @param jobInfo
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody JobInfo jobInfo) {
        boolean result = jobInfoService.insert(jobInfo);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param jobInfo
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody JobInfo jobInfo) {
        boolean result = jobInfoService.update(jobInfo);
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
        int result = jobInfoService.delete(id);
        return R.ok(result);
    }

}