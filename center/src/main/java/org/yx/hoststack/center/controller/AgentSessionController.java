package org.yx.hoststack.center.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.entity.AgentSession;
import org.yx.hoststack.center.service.AgentSessionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

/**
 * HOST/CONTAINER-AGENT会话信息
 *
 * @author lyc
 * @since 2024-12-19 19:27:24
 */
@RestController
@RequestMapping("/agentSession")
@RequiredArgsConstructor
public class AgentSessionController {

    private AgentSessionService agentSessionService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public ResponseEntity<Page<AgentSession>> findPage(@RequestBody AgentSession params) {
        Page<AgentSession> result = agentSessionService.findPage(params);
        return ResponseEntity.ok(result);
    }
    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public ResponseEntity<List<AgentSession>> findList(@RequestBody AgentSession params) {
        List<AgentSession> result = agentSessionService.findList(params);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<AgentSession> findById(@PathVariable("id") Long id) {
        AgentSession agentSession = agentSessionService.findById(id);
        return ResponseEntity.ok(agentSession);
    }

    /**
     * 新增
     *
     * @param agentSession
     * @return
     */
    @PostMapping
    public ResponseEntity<Boolean> insert( @RequestBody AgentSession agentSession) {
        boolean result = agentSessionService.insert(agentSession);
        return ResponseEntity.ok(result);
    }

    /**
     * 修改
     *
     * @param agentSession
     * @return
     */
    @PutMapping
    public ResponseEntity<Boolean> update( @RequestBody AgentSession agentSession) {
        boolean result = agentSessionService.update(agentSession);
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
        int result = agentSessionService.delete(id);
        return ResponseEntity.ok(result);
    }

}