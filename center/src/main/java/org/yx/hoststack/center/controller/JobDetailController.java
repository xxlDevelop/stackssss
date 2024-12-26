package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.JobDetail;
import org.yx.hoststack.center.service.JobDetailService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/jobDetail")
@RequiredArgsConstructor
public class JobDetailController {

    private final JobDetailService jobDetailService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<JobDetail>> findPage(@RequestBody JobDetail params) {
        Page<JobDetail> result = jobDetailService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<JobDetail>> findList(@RequestBody JobDetail params) {
        List<JobDetail> result = jobDetailService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<JobDetail> findById(@PathVariable("id") Long id) {
        JobDetail jobDetail = jobDetailService.findById(id);
        return R.ok(jobDetail);
    }

    /**
     * 新增
     *
     * @param jobDetail
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody JobDetail jobDetail) {
        boolean result = jobDetailService.insert(jobDetail);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param jobDetail
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody JobDetail jobDetail) {
        boolean result = jobDetailService.update(jobDetail);
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
        int result = jobDetailService.delete(id);
        return R.ok(result);
    }

}