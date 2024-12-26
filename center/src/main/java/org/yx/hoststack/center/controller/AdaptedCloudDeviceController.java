package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.yx.lib.utils.util.R;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.AdaptedCloudDevice;
import org.yx.hoststack.center.service.AdaptedCloudDeviceService;

import java.util.List;

/**
 * 已适配的云机设备信息表
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/adaptedCloudDevice")
@RequiredArgsConstructor
public class AdaptedCloudDeviceController {

    private final AdaptedCloudDeviceService adaptedCloudDeviceService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<AdaptedCloudDevice>> findPage(@RequestBody AdaptedCloudDevice params) {
        Page<AdaptedCloudDevice> result = adaptedCloudDeviceService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<AdaptedCloudDevice>> findList(@RequestBody AdaptedCloudDevice params) {
        List<AdaptedCloudDevice> result = adaptedCloudDeviceService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<AdaptedCloudDevice> findById(@PathVariable("id") Long id) {
        AdaptedCloudDevice adaptedCloudDevice = adaptedCloudDeviceService.findById(id);
        return R.ok(adaptedCloudDevice);
    }

    /**
     * 新增
     *
     * @param adaptedCloudDevice
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody AdaptedCloudDevice adaptedCloudDevice) {
        boolean result = adaptedCloudDeviceService.insert(adaptedCloudDevice);
        return R.ok(result);
    }

    /**
     * 修改
     *
     * @param adaptedCloudDevice
     * @return
     */
    @PutMapping
    public R<Boolean> update(@RequestBody AdaptedCloudDevice adaptedCloudDevice) {
        boolean result = adaptedCloudDeviceService.update(adaptedCloudDevice);
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
        int result = adaptedCloudDeviceService.delete(id);
        return R.ok(result);
    }

}