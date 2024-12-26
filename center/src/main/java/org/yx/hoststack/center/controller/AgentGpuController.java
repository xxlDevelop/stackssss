package org.yx.hoststack.center.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.AgentGpu;
import org.yx.hoststack.center.service.AgentGpuService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

/**
 * HOST/CONTAINER-AGENT GPU信息
 *
 * @author lyc
 * @since 2024-12-19 19:27:24
 */
@RestController
@RequestMapping("/agentGpu")
@RequiredArgsConstructor
public class AgentGpuController {

    private AgentGpuService agentGpuService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public ResponseEntity<Page<AgentGpu>> findPage(@RequestBody AgentGpu params) {
        Page<AgentGpu> result = agentGpuService.findPage(params);
        return ResponseEntity.ok(result);
    }
    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public ResponseEntity<List<AgentGpu>> findList(@RequestBody AgentGpu params) {
        List<AgentGpu> result = agentGpuService.findList(params);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<AgentGpu> findById(@PathVariable("id") Long id) {
        AgentGpu agentGpu = agentGpuService.findById(id);
        return ResponseEntity.ok(agentGpu);
    }

    /**
     * 新增
     *
     * @param agentGpu
     * @return
     */
    @PostMapping
    public ResponseEntity<Boolean> insert( @RequestBody AgentGpu agentGpu) {
        boolean result = agentGpuService.insert(agentGpu);
        return ResponseEntity.ok(result);
    }

    /**
     * 修改
     *
     * @param agentGpu
     * @return
     */
    @PutMapping
    public ResponseEntity<Boolean> update( @RequestBody AgentGpu agentGpu) {
        boolean result = agentGpuService.update(agentGpu);
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
        int result = agentGpuService.delete(id);
        return ResponseEntity.ok(result);
    }

}