package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.common.constant.RequestMappingBase;
import org.yx.hoststack.center.common.req.image.CreateImageReq;
import org.yx.hoststack.center.common.req.image.ImageListReq;
import org.yx.hoststack.center.common.req.image.ImageStatusReq;
import org.yx.hoststack.center.common.req.image.UpdateImageReq;
import org.yx.hoststack.center.common.resp.PageResp;
import org.yx.hoststack.center.common.resp.image.ImageListResp;
import org.yx.hoststack.center.entity.ImageInfo;
import org.yx.hoststack.center.service.ImageInfoService;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * 镜像信息
 *
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@RestController
@RequestMapping(RequestMappingBase.admin + RequestMappingBase.image)
@RequiredArgsConstructor
public class ImageInfoController {

    private final ImageInfoService imageInfoService;

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findPage")
    public R<Page<ImageInfo>> findPage(@RequestBody ImageInfo params) {
        Page<ImageInfo> result = imageInfoService.findPage(params);
        return R.ok(result);
    }

    /**
     * 列表查询
     *
     * @param params
     * @return
     */
    @PostMapping("/findList")
    public R<List<ImageInfo>> findList(@RequestBody ImageInfo params) {
        List<ImageInfo> result = imageInfoService.findList(params);
        return R.ok(result);
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<ImageInfo> findById(@PathVariable("id") Long id) {
        ImageInfo imageInfo = imageInfoService.findById(id);
        return R.ok(imageInfo);
    }

    /**
     * 新增
     *
     * @param imageInfo
     * @return
     */
    @PostMapping
    public R<Boolean> insert(@RequestBody ImageInfo imageInfo) {
        boolean result = imageInfoService.insert(imageInfo);
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
        int result = imageInfoService.delete(id);
        return R.ok(result);
    }

    @PostMapping("/create")
    public R<?> createImage(@RequestBody @Valid CreateImageReq req) {
        return imageInfoService.createImage(req);
    }

    @PostMapping("/createImageFromRemote")
    public R<?> createImageFromRemote(@RequestBody @Valid CreateImageReq req) {
        return imageInfoService.createImageFromRemote(req);
    }

    @PostMapping("/update")
    public R<?> updateImage(@RequestBody @Valid UpdateImageReq req) {
        return imageInfoService.updateImage(req);
    }

    @PostMapping("/list")
    public R<PageResp<ImageListResp>> list(@RequestBody @Validated ImageListReq imageListReq) {
        return imageInfoService.list(imageListReq);
    }

    @PostMapping("/status")
    public R<?> updateStatus(@RequestBody @Validated ImageStatusReq req) {
        return imageInfoService.updateStatus(req);
    }

}