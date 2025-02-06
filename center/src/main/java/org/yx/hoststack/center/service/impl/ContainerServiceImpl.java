package org.yx.hoststack.center.service.impl;

import cn.hutool.log.Log;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.req.container.ContainerCreateReqDTO;
import org.yx.hoststack.center.common.req.container.ContainerPageReqDTO;
import org.yx.hoststack.center.common.resp.comtainer.ContainerCreateRespVO;
import org.yx.hoststack.center.common.resp.comtainer.ContainerPageDBVO;
import org.yx.hoststack.center.entity.*;
import org.yx.hoststack.center.mapper.ContainerMapper;
import org.yx.hoststack.center.service.*;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.SpringContextHolder;
import org.yx.lib.utils.util.StringPool;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
public class ContainerServiceImpl extends ServiceImpl<ContainerMapper, Container> implements ContainerService {

    private final ContainerMapper containerMapper;
    private final TransactionTemplate transactionTemplate;
    private final ContainerCreateProfileService containerCreateProfileService;

    public ContainerServiceImpl(ContainerMapper containerMapper,
                                TransactionTemplate transactionTemplate,
                                ContainerCreateProfileService containerCreateProfileService) {
        this.containerMapper = containerMapper;
        this.transactionTemplate = transactionTemplate;
        this.containerCreateProfileService = containerCreateProfileService;
    }

    @Override
    public IPage<ContainerPageDBVO> findPage(ContainerPageReqDTO dto) {

        return containerMapper.getPageList(dto, Page.of(dto == null || dto.getCurrent() == null || dto.getCurrent() <= 0L ? 1L : dto.getCurrent()
                , dto == null || dto.getSize() == null || dto.getSize() <= 0L ? 10L : dto.getSize()));

    }

    @Override
    public List<Container> findList(Container params) {
        LambdaQueryWrapper<Container> query = Wrappers.lambdaQuery(Container.class);
        return baseMapper.selectList(query);
    }

    @Override
    public Container findById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public ContainerCreateRespVO insert(ContainerCreateReqDTO dto) {
//        return save(dto);
        return null;
    }

    @Override
    public boolean update(Container container) {
        return updateById(container);
    }

    @Override
    public int delete(Long id) {
        return baseMapper.deleteById(id);
    }

    @Override
    public void createContainer(String traceId, String hostId, String cid, String profile,
                                String imageId, String imageVer, String imageType, Long profileTemplateId, Long tid) {
        QueryWrapper<Container> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("host_id", hostId).last("LIMIT 1").select("MAX(sequence_number)");
        Container maxSequenceNumberByHostId = getOne(queryWrapper);
        int sequenceNumber;
        if (maxSequenceNumberByHostId != null) {
            sequenceNumber = maxSequenceNumberByHostId.getSequenceNumber() + 1;
        } else {
            sequenceNumber = 0;
        }
        transactionTemplate.executeWithoutResult(status -> {
            Container container = Container.builder()
                    .containerId(cid)
                    .hostId(hostId)
                    .sequenceNumber(sequenceNumber)
                    .imageId(imageId)
                    .imageVer(imageVer)
                    .imageType(imageType)
                    .providerTenantId(tid)
                    .build();
            this.save(container);
            ContainerCreateProfile profileConfig = ContainerCreateProfile.builder()
                    .containerId(cid)
                    .hostId(hostId)
                    .imageId(imageId)
                    .imageVer(imageVer)
                    .profileUserData(profile)
                    .profileTemplateId(profileTemplateId)
                    .memory("")//TODO
                    .vCpus(0)
                    .license("")
                    .build();
            containerCreateProfileService.save(profileConfig);
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CONTAINER_EVENT)
                    .p(LogFieldConstants.ACTION, "Created")
                    .p(LogFieldConstants.TRACE_ID, traceId)
                    .p("HostId", hostId)
                    .p("Cid", cid)
                    .i();
        });
    }

    @Override
    public void releaseContainer(String traceId, String cid) {
        transactionTemplate.executeWithoutResult(status -> {
            ContainerService containerService = SpringContextHolder.getBean(ContainerService.class);
            ContainerCreateProfileService containerCreateProfileService = SpringContextHolder.getBean(ContainerCreateProfileService.class);
            ContainerNetConfigService containerNetConfigService = SpringContextHolder.getBean(ContainerNetConfigService.class);
            ContainerProxyConfigService containerProxyConfigService = SpringContextHolder.getBean(ContainerProxyConfigService.class);

            containerService.remove(Wrappers.lambdaQuery(Container.class).eq(Container::getContainerId, cid));
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CONTAINER_EVENT)
                    .p(LogFieldConstants.ACTION, "Drop")
                    .p("Cid", cid)
                    .i();

            containerCreateProfileService.remove(Wrappers.lambdaQuery(ContainerCreateProfile.class).eq(ContainerCreateProfile::getContainerId, cid));
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CONTAINER_EVENT)
                    .p(LogFieldConstants.ACTION, "DeleteContainerProfile")
                    .p(LogFieldConstants.TRACE_ID, traceId)
                    .p("Cid", cid)
                    .i();

            containerNetConfigService.remove(Wrappers.lambdaQuery(ContainerNetConfig.class).eq(ContainerNetConfig::getContainerId, cid));
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CONTAINER_EVENT)
                    .p(LogFieldConstants.ACTION, "DeleteContainerNetConfig")
                    .p(LogFieldConstants.TRACE_ID, traceId)
                    .p("Cid", cid)
                    .i();

            containerProxyConfigService.remove(Wrappers.lambdaQuery(ContainerProxyConfig.class).eq(ContainerProxyConfig::getContainerId, cid));
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CONTAINER_EVENT)
                    .p(LogFieldConstants.ACTION, "DeleteContainerProxyConfig")
                    .p(LogFieldConstants.TRACE_ID, traceId)
                    .p("Cid", cid)
                    .i();
        });
    }

    @Override
    public void releaseContainerByHost(String traceId, String hostId) {
        List<String> cIds = this.list(Wrappers.lambdaQuery(Container.class).eq(Container::getHostId, hostId)
                .select(Container::getContainerId)).stream().map(Container::getContainerId).toList();
        if (cIds.isEmpty()) {
            return;
        }
        transactionTemplate.executeWithoutResult(status -> {
            ContainerService containerService = SpringContextHolder.getBean(ContainerService.class);
            ContainerCreateProfileService containerCreateProfileService = SpringContextHolder.getBean(ContainerCreateProfileService.class);
            ContainerNetConfigService containerNetConfigService = SpringContextHolder.getBean(ContainerNetConfigService.class);
            ContainerProxyConfigService containerProxyConfigService = SpringContextHolder.getBean(ContainerProxyConfigService.class);


            containerService.remove(Wrappers.lambdaQuery(Container.class).in(Container::getContainerId, cIds));
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CONTAINER_EVENT)
                    .p(LogFieldConstants.ACTION, "Drop")
                    .p(LogFieldConstants.TRACE_ID, traceId)
                    .p("CIds", String.join(StringPool.COMMA, cIds))
                    .p("HostId", hostId)
                    .i();

            containerCreateProfileService.remove(Wrappers.lambdaQuery(ContainerCreateProfile.class).in(ContainerCreateProfile::getContainerId, cIds));
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CONTAINER_EVENT)
                    .p(LogFieldConstants.ACTION, "DeleteContainerProfile")
                    .p(LogFieldConstants.TRACE_ID, traceId)
                    .p("CIds", String.join(StringPool.COMMA, cIds))
                    .p("HostId", hostId)
                    .i();

            containerNetConfigService.remove(Wrappers.lambdaQuery(ContainerNetConfig.class).in(ContainerNetConfig::getContainerId, cIds));
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CONTAINER_EVENT)
                    .p(LogFieldConstants.ACTION, "DeleteContainerNetConfig")
                    .p(LogFieldConstants.TRACE_ID, traceId)
                    .p("CIds", String.join(StringPool.COMMA, cIds))
                    .p("HostId", hostId)
                    .i();

            containerProxyConfigService.remove(Wrappers.lambdaQuery(ContainerProxyConfig.class).in(ContainerProxyConfig::getContainerId, cIds));
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CONTAINER_EVENT)
                    .p(LogFieldConstants.ACTION, "DeleteContainerProxyConfig")
                    .p(LogFieldConstants.TRACE_ID, traceId)
                    .p("CIds", String.join(StringPool.COMMA, cIds))
                    .p("HostId", hostId)
                    .i();
        });
    }

    @Override
    public void upgradeContainer(String traceId, String cid, String imageId, String imageVer) {
        this.update(Wrappers.lambdaUpdate(Container.class)
                .set(Container::getImageVer, imageVer)
                .eq(Container::getContainerId, cid)
                .eq(Container::getImageId, imageId)
        );
        // TODO update container create profile
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, CenterEvent.CONTAINER_EVENT)
                .p(LogFieldConstants.ACTION, "Upgrade")
                .p(LogFieldConstants.TRACE_ID, traceId)
                .p("Cid", cid)
                .p("ImageId", imageId)
                .p("newImageVer", imageVer)
                .i();
    }
}