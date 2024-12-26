package org.yx.hoststack.center.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.ServiceDetail;
import org.yx.hoststack.center.service.ServiceDetailService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

/**
 * IDC服务或者中继节点服务信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@RestController
@RequestMapping("/serviceDetail")
@RequiredArgsConstructor
public class ServiceDetailController {

    private ServiceDetailService serviceDetailService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public ResponseEntity<Page<ServiceDetail>> findPage(@RequestBody ServiceDetail params) {
        Page<ServiceDetail> result = serviceDetailService.findPage(params);
        return ResponseEntity.ok(result);
    }
    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public ResponseEntity<List<ServiceDetail>> findList(@RequestBody ServiceDetail params) {
        List<ServiceDetail> result = serviceDetailService.findList(params);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceDetail> findById(@PathVariable("id") Long id) {
        ServiceDetail serviceDetail = serviceDetailService.findById(id);
        return ResponseEntity.ok(serviceDetail);
    }

    /**
     * 新增
     *
     * @param serviceDetail
     * @return
     */
    @PostMapping
    public ResponseEntity<Boolean> insert( @RequestBody ServiceDetail serviceDetail) {
        boolean result = serviceDetailService.insert(serviceDetail);
        return ResponseEntity.ok(result);
    }

    /**
     * 修改
     *
     * @param serviceDetail
     * @return
     */
    @PutMapping
    public ResponseEntity<Boolean> update( @RequestBody ServiceDetail serviceDetail) {
        boolean result = serviceDetailService.update(serviceDetail);
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
        int result = serviceDetailService.delete(id);
        return ResponseEntity.ok(result);
    }

}