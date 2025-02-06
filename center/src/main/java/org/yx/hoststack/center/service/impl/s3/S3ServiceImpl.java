package org.yx.hoststack.center.service.impl.s3;

import com.alibaba.fastjson.JSON;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.google.common.collect.Maps;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.common.dto.OssConfigDetail;
import org.yx.hoststack.center.common.dto.S3ClientWrapper;
import org.yx.hoststack.center.common.req.storage.GetDownloadUrlReq;
import org.yx.hoststack.center.common.req.storage.GetUploadUrlReq;
import org.yx.hoststack.center.common.resp.storage.GetDownloadUrlResp;
import org.yx.hoststack.center.common.resp.storage.GetUploadUrlRequestResp;
import org.yx.hoststack.center.common.utils.LoginUtil;
import org.yx.hoststack.center.service.OssConfigService;
import org.yx.hoststack.center.service.s3.S3Service;
import org.yx.lib.utils.constant.CommonConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.util.R;

import java.net.URL;
import java.util.Date;
import java.util.Map;

import static org.yx.hoststack.center.common.constant.CenterEvent.Action.S3_GENERATE_UPLOAD_URL_FAIL;
import static org.yx.hoststack.center.common.enums.SysCode.x00000528;
import static org.yx.hoststack.center.common.enums.SysCode.x00000529;
import static org.yx.lib.utils.logger.LogFieldConstants.*;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final Map<String, S3ClientWrapper> s3ClientMap = Maps.newConcurrentMap();
    private final OssConfigService ossConfigService;


    @Override
    public R<?> generateUploadUrl(GetUploadUrlReq getUploadUrlReq) {
        try {
            AmazonS3 s3Client = getS3Client(getUploadUrlReq.getRegion());
            OssConfigDetail ossConfigDetail = getOssConfig(getUploadUrlReq.getRegion());
            String objectKey = String.format("%s/%s", LoginUtil.getTenantId(), getUploadUrlReq.getFileId());

            Date expiration = new Date(System.currentTimeMillis() + ossConfigDetail.getTokenDurationSeconds() * 1000);
            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
                    ossConfigDetail.getBucket(),
                    objectKey,
                    HttpMethod.PUT
            ).withExpiration(expiration);
            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
            return R.ok(GetUploadUrlRequestResp.builder()
                    .uploadUrl(url.toString())
                    .build());
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(EVENT, S3_GENERATE_UPLOAD_URL_FAIL)
                    .p(ERR_MSG, e.getMessage())
                    .p(Alarm, 0)
                    .p(TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                    .p(ReqData, JSON.toJSONString(getUploadUrlReq))
                    .e(e);
            return R.failed(x00000528.getValue(), x00000528.getMsg());
        }
    }

    @Override
    public R<?> generateDownloadUrl(GetDownloadUrlReq getDownloadUrlReq) {
        try {
            // Obtain the S3 client and configuration
            AmazonS3 s3Client = getS3Client(getDownloadUrlReq.getRegion());
            OssConfigDetail ossConfigDetail = getOssConfig(getDownloadUrlReq.getRegion());

            // Build object key (Add tenant isolation)
            String objectKey = String.format("%s/%s", LoginUtil.getTenantId(), getDownloadUrlReq.getFileId());

            // SET THE URL EXPIRATION TIME
            Date expiration = new Date(System.currentTimeMillis() + ossConfigDetail.getTokenDurationSeconds() * 1000);

            // Generate a pre-signed download URL
            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
                    ossConfigDetail.getBucket(),
                    objectKey,
                    HttpMethod.GET
            ).withExpiration(expiration);

            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

            return R.ok(GetDownloadUrlResp.builder()
                    .downloadUrl(url.toString())
                    .build());

        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(EVENT, "S3_GENERATE_DOWNLOAD_URL_FAIL")
                    .p(ERR_MSG, e.getMessage())
                    .p(Alarm, 0)
                    .p(TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                    .p(ReqData, JSON.toJSONString(getDownloadUrlReq))
                    .e(e);
            return R.failed(x00000529.getValue(), x00000529.getMsg());
        }
    }

    /**
     * GETS OR CREATES AN S3 CLIENT WRAPPER
     */
    private S3ClientWrapper getOrCreateS3ClientWrapper(String region) {
        return s3ClientMap.computeIfAbsent(region, k -> {
            try {
                OssConfigDetail config = ossConfigService.getOssConfigByRegion(region);
                AmazonS3 s3Client = createS3Client(config);
                return new S3ClientWrapper(s3Client, config);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create S3 client: " + e.getMessage());
            }
        });
    }

    /**
     * Obtain an S3 client
     */
    public AmazonS3 getS3Client(String region) {
        return getOrCreateS3ClientWrapper(region).getS3Client();
    }

    /**
     * Get configuration information
     */
    public OssConfigDetail getOssConfig(String region) {
        return getOrCreateS3ClientWrapper(region).getConfigDetail();
    }

    /**
     * Creating an S3 client
     */
    private AmazonS3 createS3Client(OssConfigDetail config) {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .withRegion(config.getRegion())
                // 根据配置决定是否使用自定义端点
//                .withEndpointConfiguration(
//                        StringUtil.isNotBlank(config.getEndpoint()) ?
//                                new AmazonS3ClientBuilder.EndpointConfiguration(
//                                        config.getEndpoint(),
//                                        config.getRegion()
//                                ) : null
//                )
                .withPathStyleAccessEnabled(true)
                .build();
    }

    /**
     * Refresh the client and configuration of the specified region
     */
    public void refreshClient(String region) {
        try {
            OssConfigDetail config = ossConfigService.getOssConfigByRegion(region);
            AmazonS3 s3Client = createS3Client(config);
            s3ClientMap.put(region, new S3ClientWrapper(s3Client, config));
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh S3 client: " + e.getMessage());
        }
    }

    /**
     * Use example method
     */
    public String generatePresignedUrl(String region, String bucket, String key) {
        S3ClientWrapper wrapper = getOrCreateS3ClientWrapper(region);
        AmazonS3 s3Client = wrapper.getS3Client();
        OssConfigDetail config = wrapper.getConfigDetail();

        // Generate pre-signed urls using configuration information and clients
        Date expiration = new Date(System.currentTimeMillis() + config.getTokenDurationSeconds() * 1000);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key)
                .withExpiration(expiration);

        return s3Client.generatePresignedUrl(request).toString();
    }

    /**
     * 清理方法
     */
    @PreDestroy
    public void cleanup() {
        s3ClientMap.values().forEach(wrapper -> {
            try {
                if (wrapper.getS3Client() != null) {
                    wrapper.getS3Client().shutdown();
                }
            } catch (Exception e) {
                KvLogger.instance(this)
                        .p(EVENT, "S3ClientCleanup")
                        .p(ERR_MSG, e.getMessage())
                        .p(Alarm, 0)
                        .p(TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                        .p("region", wrapper.getConfigDetail().getRegion())
                        .p("message", e.getMessage())
                        .e(e);
            }
        });
        s3ClientMap.clear();
    }


}