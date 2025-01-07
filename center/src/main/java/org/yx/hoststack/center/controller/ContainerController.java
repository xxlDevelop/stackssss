package org.yx.hoststack.center.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.yx.hoststack.center.common.constant.RequestAttributeConstants;
import org.yx.hoststack.center.common.req.container.ContainerCreateReqDTO;
import org.yx.hoststack.center.common.req.container.ContainerPageReqDTO;
import org.yx.hoststack.center.common.resp.comtainer.ContainerCreateRespVO;
import org.yx.hoststack.center.common.resp.comtainer.ContainerSimpleVO;
import org.yx.hoststack.center.common.resp.comtainer.wrappers.ContainerSimpleVOWrapper;
import org.yx.hoststack.center.entity.AgentSession;
import org.yx.hoststack.center.entity.Host;
import org.yx.hoststack.center.entity.ImageInfo;
import org.yx.hoststack.center.service.*;
import org.yx.hoststack.center.service.jobinfo.CreateContainerJobService;
import org.yx.lib.utils.util.R;

import java.util.List;
import java.util.Optional;

import static org.yx.hoststack.center.common.enums.SysCode.*;

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
    private final CreateContainerJobService createContainerJobService;
    private final ImageInfoService imageInfoService;
    private final HostService hostService;
    private final AgentSessionService agentSessionService;
    private final IdcNetConfigService idcNetConfigService;

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

    /**
     * create container
     *
     * @param dto request data
     * @return result
     */
    @PostMapping("/create")
    public R<ContainerCreateRespVO> insert(@RequestAttribute(RequestAttributeConstants.TENANT_ID) Long tenantId, @Valid @Validated @RequestBody ContainerCreateReqDTO dto) {
        // check image is ready
        Optional<ImageInfo> imageInfoOptional = imageInfoService.getOneOpt(Wrappers.<ImageInfo>lambdaQuery().eq(ImageInfo::getImageId, dto.getImageId()).eq(ImageInfo::getIsEnabled, true));
        if (imageInfoOptional.isEmpty()) {
            return R.<ContainerCreateRespVO>builder().code(x00000420.getValue()).data(null).msg(x00000420.getMsg()).build();
        }
        Optional<Host> hostOptional = hostService.getOptById(dto.getHostId());
        if (hostOptional.isEmpty()) {
            return R.<ContainerCreateRespVO>builder().code(x00000430.getValue()).data(null).msg(x00000430.getMsg()).build();
        }
        Host host = hostOptional.get();
        if (!host.getRegion().equals(dto.getRegion())) {
            return R.<ContainerCreateRespVO>builder().code(x00000450.getValue()).data(null).msg(x00000450.getMsg()).build();
        }
        if (!agentSessionService.exists(Wrappers.<AgentSession>lambdaQuery().eq(AgentSession::getAgentId, dto.getHostId()))) {
            return R.<ContainerCreateRespVO>builder().code(x00000440.getValue()).data(null).msg(x00000440.getMsg()).build();
        }
        // Query available ContainerIPs
        List<String> containerIps = idcNetConfigService.listAvailableIpsByIdcLimitCount(host.getIdc(), dto.getCount());
        if (containerIps == null || containerIps.isEmpty()) {
            return R.<ContainerCreateRespVO>builder().code(x00000100.getValue()).data(null).msg(x00000100.getMsg()).build();
        }
        if (containerIps.size() < dto.getCount()) {
            return R.<ContainerCreateRespVO>builder().code(x00000101.getValue()).data(null).msg(x00000101.getMsg()).build();
        }
        return R.ok(createContainerJobService.insertContainerCreateJobs(tenantId, dto, imageInfoOptional.get(), host, containerIps));
    }

}