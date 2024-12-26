package org.yx.hoststack.center.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.AgentCpu;
import org.yx.hoststack.center.service.AgentCpuService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

/**
 * HOST/CONTAINER-AGENT CPU信息
 *
 * @author lyc
 * @since 2024-12-19 19:27:24
 */
@RestController
@RequestMapping("/agentCpu")
@RequiredArgsConstructor
public class AgentCpuController {

    private AgentCpuService agentCpuService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public ResponseEntity<Page<AgentCpu>> findPage(@RequestBody AgentCpu params) {
        Page<AgentCpu> result = agentCpuService.findPage(params);
        return ResponseEntity.ok(result);
    }
    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public ResponseEntity<List<AgentCpu>> findList(@RequestBody AgentCpu params) {
        List<AgentCpu> result = agentCpuService.findList(params);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<AgentCpu> findById(@PathVariable("id") Long id) {
        AgentCpu agentCpu = agentCpuService.findById(id);
        return ResponseEntity.ok(agentCpu);
    }

    /**
     * 新增
     *
     * @param agentCpu
     * @return
     */
    @PostMapping
    public ResponseEntity<Boolean> insert( @RequestBody AgentCpu agentCpu) {
        boolean result = agentCpuService.insert(agentCpu);
        return ResponseEntity.ok(result);
    }

    /**
     * 修改
     *
     * @param agentCpu
     * @return
     */
    @PutMapping
    public ResponseEntity<Boolean> update( @RequestBody AgentCpu agentCpu) {
        boolean result = agentCpuService.update(agentCpu);
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
        int result = agentCpuService.delete(id);
        return ResponseEntity.ok(result);
    }

}