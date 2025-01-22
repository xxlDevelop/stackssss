package org.yx.hoststack.center.service.impl;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yx.hoststack.center.apiservice.ApiServiceBase;
import org.yx.hoststack.center.common.config.channel.ChannelSendConfig;
import org.yx.hoststack.center.common.enums.JobSubTypeEnum;
import org.yx.hoststack.center.common.enums.JobTypeEnum;
import org.yx.hoststack.center.common.exception.ImageException;
import org.yx.hoststack.center.common.req.channel.SendChannelBasic;
import org.yx.hoststack.center.common.req.image.CreateImageReq;
import org.yx.hoststack.center.common.req.image.ImageListReq;
import org.yx.hoststack.center.common.req.image.ImageStatusReq;
import org.yx.hoststack.center.common.req.image.UpdateImageReq;
import org.yx.hoststack.center.common.resp.PageResp;
import org.yx.hoststack.center.common.resp.image.CreateImageResp;
import org.yx.hoststack.center.common.resp.image.ImageListResp;
import org.yx.hoststack.center.entity.ImageInfo;
import org.yx.hoststack.center.jobs.JobManager;
import org.yx.hoststack.center.jobs.cmd.JobCmd;
import org.yx.hoststack.center.jobs.cmd.image.CreateImageCmdData;
import org.yx.hoststack.center.mapper.ImageInfoMapper;
import org.yx.hoststack.center.service.ImageInfoService;
import org.yx.hoststack.center.service.center.CenterService;
import org.yx.lib.utils.constant.CommonConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.StringUtil;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.yx.hoststack.center.common.constant.CenterEvent.Action.*;
import static org.yx.hoststack.center.common.constant.CenterEvent.CREATE_IMAGE_EVENT;
import static org.yx.hoststack.center.common.enums.SysCode.*;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImageInfoServiceImpl extends ServiceImpl<ImageInfoMapper, ImageInfo> implements ImageInfoService {

    private final ImageInfoMapper imageInfoMapper;
    private final JobManager jobManager;
    private final CenterService centerService;
    private final ChannelSendConfig channelSendConfig;
    private final ApiServiceBase apiServiceBase;

    @Override
    public Page<ImageInfo> findPage(ImageInfo params) {
        Page<ImageInfo> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<ImageInfo> query = Wrappers.lambdaQuery(ImageInfo.class);
        return imageInfoMapper.selectPage(page, query);
    }

    @Override
    public List<ImageInfo> findList(ImageInfo params) {
        LambdaQueryWrapper<ImageInfo> query = Wrappers.lambdaQuery(ImageInfo.class);
        return imageInfoMapper.selectList(query);
    }

    @Override
    public ImageInfo findById(Long id) {
        return imageInfoMapper.selectById(id);
    }

    @Override
    public boolean insert(ImageInfo imageInfo) {
        return save(imageInfo);
    }

    @Override
    public boolean update(ImageInfo imageInfo) {
        return updateById(imageInfo);
    }

    @Override
    public int delete(Long id) {
        return imageInfoMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<?> createImage(CreateImageReq req) {
        // If no value is passed, all servers are forwarded
        if (StringUtil.isBlank(req.getRelay()) && StringUtil.isBlank(req.getIdc())) {
            //TODO send all servers
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CREATE_IMAGE_EVENT)
                    .p(LogFieldConstants.ACTION, SEND_ALL_SERVER)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .d();
            return R.ok();
        }
        String serviceId = StringUtil.isNotBlank(req.getIdc()) ? req.getIdc() : req.getRelay();
        Optional<Channel> relayChannel = Optional.ofNullable(centerService.findLocalChannel(SendChannelBasic.builder().serviceId(serviceId).build()));
        if (relayChannel.isPresent()) {
            try {
                return createImageRespR(req);
            } catch (ImageException e) {
                return R.failed(e.getCode(), e.getMessage());
            } catch (Exception e) {
                return R.failed(x00000507.getValue(), x00000507.getMsg());
            }
        }
        // post remote
        String postUrl = centerService.buildRemoteUrl(SendChannelBasic.builder().serviceId(serviceId).build(), channelSendConfig.getCreateImageUrl());
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, CREATE_IMAGE_EVENT)
                .p(LogFieldConstants.ACTION, SEND_REMOTE_SERVER)
                .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                .p(LogFieldConstants.ReqUrl, postUrl)
                .d();
        return apiServiceBase.post(postUrl, MDC.get(CommonConstants.TRACE_ID), centerService.prepareRequestHeaders(), JSON.toJSONString(req))
                .map(result -> JSON.parseObject(result, R.class))
                .doOnError(e -> logError(e, postUrl, req))
                .onErrorReturn(R.failed(x00000506.getValue(), x00000506.getMsg()))
                .doOnNext(r -> logSuccess(r, postUrl, req)).block();
    }

    public void logError(Throwable e, String remoteUrl, CreateImageReq req) {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, CREATE_IMAGE_EVENT)
                .p(LogFieldConstants.ACTION, FETCH_CHANNEL_FROM_REMOTE_FAILED)
                .p(LogFieldConstants.ERR_MSG, e.getMessage())
                .p(LogFieldConstants.Alarm, 0)
                .p(LogFieldConstants.API_URL, remoteUrl)
                .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                .p(LogFieldConstants.TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                .e(e);
    }

    public void logSuccess(R<?> r, String remoteUrl, CreateImageReq req) {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, CREATE_IMAGE_EVENT)
                .p(LogFieldConstants.ACTION, CREATE_IMAGE_POST_REMOTE_SUCCESS)
                .p(LogFieldConstants.API_URL, remoteUrl)
                .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                .p(LogFieldConstants.RespData, JSON.toJSONString(r))
                .p(LogFieldConstants.TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                .i();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<?> createImageFromRemote(CreateImageReq req) {
        try {
            return createImageRespR(req);
        } catch (ImageException e) {
            return R.failed(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return R.failed(x00000507.getValue(), x00000507.getMsg());
        }
    }

    public R<CreateImageResp> createImageRespR(CreateImageReq req) {
        try {
            // Build image information
            ImageInfo imageInfo = buildImageInfo(req);
            // Saving image information
            saveOrUpdate(imageInfo);
            // Asynchronously send the create image command to the partition
            String jobId = sendCreateImageInstruction(req, imageInfo);

            return R.ok(CreateImageResp.builder().jobId(jobId).build());
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CREATE_IMAGE_EVENT)
                    .p(LogFieldConstants.ACTION, CREATE_IMAGE_FAIL)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .e();

            // If it is a specific error, throw a business exception
            if (e.getMessage().contains("Image with MD5")) {
                throw new ImageException(x00000508.getValue(), e.getMessage(), e);
            }
            // Throw other exceptions
            throw new ImageException(x00000507.getValue(), x00000507.getMsg(), e);
        }
    }

    public ImageInfo buildImageInfo(CreateImageReq req) {
        ImageInfo existingImage = getByMd5(req.getMd5());
        if (existingImage != null) {
            // Mirror repetition
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CREATE_IMAGE_EVENT)
                    .p(LogFieldConstants.ACTION, DUPLICATE_IMAGE_FOUNT)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                    .p("ExistingImageId", existingImage.getImageId())
                    .w();

            throw new RuntimeException(String.format(x00000508.getMsg(), req.getMd5()));
        }

        String imageId = UUID.fastUUID().toString();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return ImageInfo.builder()
                .imageId(imageId)
                .imageName(req.getImageName())
                .imageVer(req.getImageVer())
                .bizType(req.getBizType())
                .resourcePool(req.getResourcePool())
                .osType(req.getOsType())
                .contianerType(req.getContianerType())
                .storagePath(req.getStoragePath())
                .downloadUrl(req.getDownloadUrl())
                .label(req.getLabel())
                .md5(req.getMd5())
                .tenantId(req.getTenantId())
                .isOfficial(req.getIsOfficial())
                .isEnabled(false)
                .createAt(now)
                .lastUpdateAt(now)
                .lastUpldateAccount("")// TODO lastUpldateAccount
                .build();
    }

    public ImageInfo buildUpdateImageInfo(UpdateImageReq req) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return ImageInfo.builder()
                .id(req.getId())
                .imageName(req.getImageName())
                .imageVer(req.getImageVer())
                .bizType(req.getBizType())
                .resourcePool(req.getResourcePool())
                .osType(req.getOsType())
                .contianerType(req.getContianerType())
                .storagePath(req.getStoragePath())
                .downloadUrl(req.getDownloadUrl())
                .label(req.getLabel())
                .md5(req.getMd5())
                .tenantId(req.getTenantId())
                .isOfficial(req.getIsOfficial())
                .isEnabled(false)
                .lastUpdateAt(now)
                .lastUpldateAccount("")// TODO lastUpldateAccount
                .build();
    }

    public String sendCreateImageInstruction(CreateImageReq req, ImageInfo imageInfo) {

        JobCmd<CreateImageCmdData> jobCmd = JobCmd.<CreateImageCmdData>builder()
                // Set job type to Image
                .jobType(JobTypeEnum.Image)
                // Set job subtype to CREATE
                .jobSubType(JobSubTypeEnum.CREATE)
                // Set job data
                .jobData(CreateImageCmdData.builder()
                        .imageId(imageInfo.getImageId())
                        .imageName(req.getImageName())
                        .imageVer(req.getImageVer())
                        .downloadUrl(req.getDownloadUrl())
                        .md5(imageInfo.getMd5())
                        .idc(req.getIdc())
                        .bucket("images")// TODO bucket
                        .build())
                // Set target location parameters (optional)
                .region(req.getRegion())
                .relay(req.getRelay())
                .idc(req.getIdc())
                // Set tenant ID
                .tenantId(req.getTenantId())
                // Zone is optional, set to null if not needed
                .zone(req.getZone())
                .build();

        return jobManager.createJob(jobCmd);

    }

    @Override
    public ImageInfo getByMd5(String md5) {
        if (StringUtil.isBlank(md5)) {
            return null;
        }

        LambdaQueryWrapper<ImageInfo> queryWrapper = Wrappers.lambdaQuery(ImageInfo.class)
                .eq(ImageInfo::getMd5, md5)
                .orderByDesc(ImageInfo::getCreateAt)
                .last("LIMIT 1");

        return getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<?> updateImage(UpdateImageReq req) {
        ImageInfo imageInfo = buildUpdateImageInfo(req);
        if (updateById(imageInfo)) {
            return R.ok();
        }
        return R.failed(x00000509.getValue(), x00000509.getMsg());
    }

    @Override
    public R<PageResp<ImageListResp>> list(ImageListReq imageListReq) {
        IPage<ImageInfo> page = new Page<>(imageListReq.getCurrent(), imageListReq.getSize());

        LambdaQueryWrapper<ImageInfo> queryWrapper = Wrappers.lambdaQuery(ImageInfo.class);

        // Add conditional filters
        if (StringUtil.isNotBlank(imageListReq.getBizType())) {
            queryWrapper.eq(ImageInfo::getBizType, imageListReq.getBizType());
        }
        if (StringUtil.isNotBlank(imageListReq.getResourcePool())) {
            queryWrapper.eq(ImageInfo::getResourcePool, imageListReq.getResourcePool());
        }
        if (StringUtil.isNotBlank(imageListReq.getOsType())) {
            queryWrapper.eq(ImageInfo::getOsType, imageListReq.getOsType());
        }
        if (StringUtil.isNotBlank(imageListReq.getContianerType())) {
            queryWrapper.eq(ImageInfo::getContianerType, imageListReq.getContianerType());
        }
        if (StringUtil.isNotBlank(imageListReq.getLabel())) {
            queryWrapper.eq(ImageInfo::getLabel, imageListReq.getLabel());
        }
        if (imageListReq.getTenantId() != null) {
            queryWrapper.eq(ImageInfo::getTenantId, imageListReq.getTenantId());
        }
        if (imageListReq.getIsOfficial() != null) {
            queryWrapper.eq(ImageInfo::getIsOfficial, imageListReq.getIsOfficial());
        }
        if (imageListReq.getIsEnabled() != null) {
            queryWrapper.eq(ImageInfo::getIsEnabled, imageListReq.getIsEnabled());
        }

        // Execute query
        page(page, queryWrapper);

        // Convert to response
        PageResp<ImageListResp> resultData = new PageResp<>();
        resultData.setCurrent(imageListReq.getCurrent());
        resultData.setSize(imageListReq.getSize());
        resultData.setTotal(page.getTotal());
        resultData.setPages(page.getPages());
        resultData.setRecords(page.getRecords().stream().map(ImageListResp::new).toList());

        return R.ok(resultData);
    }

    @Override
    public R<?> updateStatus(ImageStatusReq req) {
        LambdaUpdateWrapper<ImageInfo> updateWrapper = Wrappers.lambdaUpdate(ImageInfo.class)
                .eq(ImageInfo::getImageId, req.getImageId())
                .set(ImageInfo::getIsEnabled, req.getIsEnabled());

        boolean success = update(updateWrapper);

        if (!success) {
            return R.failed(x00000510.getValue(), x00000510.getMsg());
        }

        return R.ok();
    }

}