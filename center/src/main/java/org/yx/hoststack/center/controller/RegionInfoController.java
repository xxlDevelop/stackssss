package org.yx.hoststack.center.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.RegionInfo;
import org.yx.hoststack.center.service.RegionInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

/**
 * 区域信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@RestController
@RequestMapping("/regionInfo")
@RequiredArgsConstructor
public class RegionInfoController {

    private RegionInfoService regionInfoService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public ResponseEntity<Page<RegionInfo>> findPage(@RequestBody RegionInfo params) {
        Page<RegionInfo> result = regionInfoService.findPage(params);
        return ResponseEntity.ok(result);
    }
    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public ResponseEntity<List<RegionInfo>> findList(@RequestBody RegionInfo params) {
        List<RegionInfo> result = regionInfoService.findList(params);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<RegionInfo> findById(@PathVariable("id") Long id) {
        RegionInfo regionInfo = regionInfoService.findById(id);
        return ResponseEntity.ok(regionInfo);
    }

    /**
     * 新增
     *
     * @param regionInfo
     * @return
     */
    @PostMapping
    public ResponseEntity<Boolean> insert( @RequestBody RegionInfo regionInfo) {
        boolean result = regionInfoService.insert(regionInfo);
        return ResponseEntity.ok(result);
    }

    /**
     * 修改
     *
     * @param regionInfo
     * @return
     */
    @PutMapping
    public ResponseEntity<Boolean> update( @RequestBody RegionInfo regionInfo) {
        boolean result = regionInfoService.update(regionInfo);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Integer> delete(@PathVariable("id") Long id) {
        int result = regionInfoService.delete(id);
        return ResponseEntity.ok(result);
    }

}