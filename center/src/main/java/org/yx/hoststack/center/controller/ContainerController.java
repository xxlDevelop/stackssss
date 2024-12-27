package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yx.hoststack.center.common.req.container.ContainerCreateReqDTO;
import org.yx.hoststack.center.common.req.container.ContainerPageReqDTO;
import org.yx.hoststack.center.common.resp.comtainer.ContainerCreateRespVO;
import org.yx.hoststack.center.common.resp.comtainer.ContainerSimpleVO;
import org.yx.hoststack.center.common.resp.comtainer.wrappers.ContainerSimpleVOWrapper;
import org.yx.hoststack.center.service.ContainerService;
import org.yx.lib.utils.util.R;

/**
 * 容器信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping("/v1/container")
@RequiredArgsConstructor
public class ContainerController {

    private final ContainerService containerService;

    /**
     * query container page list v1
     *
     * @param dto request
     * @return result
     */
    @PostMapping("/list")
    public R<IPage<ContainerSimpleVO>> findPage(@Valid @Validated @RequestBody ContainerPageReqDTO dto) {
        return R.ok(ContainerSimpleVOWrapper.build().pageVO(containerService.findPage(dto)));
    }


//    /**
//     * 查询
//     *
//     * @param id
//     * @return
//     */
//    @GetMapping("/{id}")
//    public R<Container> findById(@PathVariable("id") Long id) {
//        Container container = containerService.findById(id);
//        return R.ok(container);
//    }

    /**
     * create container
     *
     * @param dto
     * @return result
     */
    @PostMapping("/create")
    public R<ContainerCreateRespVO> insert(@RequestBody ContainerCreateReqDTO dto) {
        return R.ok(containerService.insert(dto));
    }

//    /**
//     * 修改
//     *
//     * @param container
//     * @return
//     */
//    @PutMapping
//    public R<Boolean> update(@RequestBody Container container) {
//        boolean result = containerService.update(container);
//        return R.ok(result);
//    }
//
//    /**
//     * 删除
//     *
//     * @param id
//     * @return
//     */
//    @DeleteMapping("/{id}")
//    public R<Integer> delete(@PathVariable("id") Long id) {
//        int result = containerService.delete(id);
//        return R.ok(result);
//    }

}