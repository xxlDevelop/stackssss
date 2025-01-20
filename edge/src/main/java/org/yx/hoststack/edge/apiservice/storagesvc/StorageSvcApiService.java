package org.yx.hoststack.edge.apiservice.storagesvc;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.net.url.UrlBuilder;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.yx.hoststack.edge.apiservice.ApiServiceBase;
import org.yx.hoststack.edge.apiservice.storagesvc.req.CreateBaseVolumeReq;
import org.yx.hoststack.edge.apiservice.storagesvc.req.CreateUserVolumeReq;
import org.yx.hoststack.edge.apiservice.storagesvc.req.DeleteVolumeReq;
import org.yx.hoststack.edge.apiservice.storagesvc.req.DistributeFileReq;
import org.yx.hoststack.edge.apiservice.storagesvc.resp.GetUserInfoResp;
import org.yx.hoststack.edge.apiservice.storagesvc.resp.ListBucketResp;
import org.yx.hoststack.edge.cache.IdcConfigCacheManager;
import org.yx.lib.utils.util.R;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;

@Service
public class StorageSvcApiService extends ApiServiceBase {
    private final IdcConfigCacheManager idcConfigCacheManager;
    private String storageSvcBaseUrl;

    public StorageSvcApiService(WebClient webClient, IdcConfigCacheManager idcConfigCacheManager,
                                @Value("${storageSvcBaseUrl}") String storageSvcBaseUrl) {
        super(webClient);
        this.idcConfigCacheManager = idcConfigCacheManager;
        this.storageSvcBaseUrl = storageSvcBaseUrl;
    }

    public Mono<R<?>> createUser(String name, Integer tid) {
        String createBucketUrl = UrlBuilder.ofHttp(storageSvcBaseUrl).addPath("/storage/objetUser/create").build();
        return post(createBucketUrl, UUID.fastUUID().toString(), null,
                MapBuilder.create(new HashMap<String, Object>())
                        .put("name", name)
                        .put("tid", tid)
                        .build()).map(result -> JSON.parseObject(result, new TypeReference<R<?>>() {
        }));
    }

    public Mono<R<?>> deleteUser(Integer tid) {
        String createBucketUrl = UrlBuilder.ofHttp(storageSvcBaseUrl).addPath("/storage/objetUser/delete").build();
        return post(createBucketUrl, UUID.fastUUID().toString(), null,
                MapBuilder.create(new HashMap<String, Object>())
                        .put("tid", tid)
                        .build()).map(result -> JSON.parseObject(result, new TypeReference<R<?>>() {
        }));
    }

    public Mono<R<GetUserInfoResp>> getUser(Integer tid) {
        String createBucketUrl = UrlBuilder.ofHttp(storageSvcBaseUrl).addPath("/storage/objetUser/detail").build();
        return post(createBucketUrl, UUID.fastUUID().toString(), null,
                MapBuilder.create(new HashMap<String, Object>())
                        .put("tid", tid)
                        .build()).map(result -> JSON.parseObject(result, new TypeReference<R<GetUserInfoResp>>() {
        }));
    }

    public Mono<R<?>> createBucket(String bucketName, Integer tid) {
        String createBucketUrl = UrlBuilder.ofHttp(storageSvcBaseUrl).addPath("/storage/bucket/create").build();
        return post(createBucketUrl, UUID.fastUUID().toString(), null,
                MapBuilder.create(new HashMap<String, Object>())
                        .put("name", bucketName)
                        .put("tid", tid)
                        .build()).map(result -> JSON.parseObject(result, new TypeReference<R<?>>() {
        }));
    }

    public Mono<R<?>> deleteBucket(String bucketName, Integer tid) {
        String deleteBucketUrl = UrlBuilder.ofHttp(storageSvcBaseUrl).addPath("/storage/bucket/delete").build();
        return post(deleteBucketUrl, UUID.fastUUID().toString(), null,
                MapBuilder.create(new HashMap<String, Object>())
                        .put("name", bucketName)
                        .put("tid", tid)
                        .build()).map(result -> JSON.parseObject(result, new TypeReference<R<?>>() {
        }));
    }

    public Mono<R<List<ListBucketResp>>> listBucket(Integer tid) {
        String listBucketUrl = UrlBuilder.ofHttp(storageSvcBaseUrl).addPath("/storage/bucket/list").build();
        return post(listBucketUrl, UUID.fastUUID().toString(), null,
                MapBuilder.create(new HashMap<String, Object>())
                        .put("tid", tid)
                        .build()).map(result -> JSON.parseObject(result, new TypeReference<R<List<ListBucketResp>>>() {
        }));
    }

    public Mono<R<?>> distributeFile(DistributeFileReq distributeFileReq, String traceId) {
        String distributeFileUrl = UrlBuilder.ofHttp(storageSvcBaseUrl).addPath("/storage/object/distribute").build();
        return post(distributeFileUrl, traceId.toString(), null,
                MapBuilder.create(new HashMap<String, Object>())
                        .put("objectKey", distributeFileReq.getObjectKey())
                        .put("downloadUrl", distributeFileReq.getDownloadUrl())
                        .put("tid", distributeFileReq.getTid())
                        .put("bucket", distributeFileReq.getBucket())
                        .put("jobID", distributeFileReq.getJobID())
                        .put("callbackUrl", distributeFileReq.getCallbackUrl())
                        .build()).map(result -> JSON.parseObject(result, new TypeReference<R<?>>() {
        }));
    }

    public Mono<R<?>> deleteObject(long tid, String bucket, String objectId, String traceId) {
        String distributeFileUrl = UrlBuilder.ofHttp(storageSvcBaseUrl).addPath("/storage/object/delete").build();
        return post(distributeFileUrl, traceId.toString(), null,
                MapBuilder.create(new HashMap<String, Object>())
                        .put("tid", tid)
                        .put("bucket", bucket)
                        .put("objectID", objectId)
                        .build()).map(result -> JSON.parseObject(result, new TypeReference<R<?>>() {
        }));
    }

    public Mono<R<?>> createBaseVolume(CreateBaseVolumeReq createBaseVolumeReq, String traceId) {
        String createBaseVolumeUrl = UrlBuilder.ofHttp(storageSvcBaseUrl).addPath("/storage/baseVolume/create").build();
        return post(createBaseVolumeUrl, traceId, null,
                MapBuilder.create(new HashMap<String, Object>())
                        .put("volumeName", createBaseVolumeReq.getVolumeName())
                        .put("size", createBaseVolumeReq.getSize())
                        .put("initMode", createBaseVolumeReq.getInitMode())
                        .put("initDownloadUrl", createBaseVolumeReq.getInitDownloadUrl())
                        .put("poolName", createBaseVolumeReq.getPoolName())
                        .put("tid", createBaseVolumeReq.getTid())
                        .build()).map(result -> JSON.parseObject(result, new TypeReference<R<?>>() {
        }));
    }

    public Mono<R<?>> deleteBaseVolume(DeleteVolumeReq deleteVolumeReq, String traceId) {
        String deleteBaseVolumeUrl = UrlBuilder.ofHttp(storageSvcBaseUrl).addPath("/storage/baseVolume/delete").build();
        return doDeleteVolume(deleteBaseVolumeUrl, deleteVolumeReq, traceId);
    }

    public Mono<String> mountBaseVolume() {
        return Mono.empty();
    }

    public Mono<R<?>> createUserVolume(CreateUserVolumeReq createUserVolumeReq, String traceId) {
        String createUserVolumeUrl = UrlBuilder.ofHttp(storageSvcBaseUrl).addPath("/storage/volume/create").build();
        return post(createUserVolumeUrl, traceId, null,
                MapBuilder.create(new HashMap<String, Object>())
                        .put("volumeName", createUserVolumeReq.getVolumeName())
                        .put("snapshotName", createUserVolumeReq.getSnapshotName())
                        .put("tid", createUserVolumeReq.getTid())
                        .build()).map(result -> JSON.parseObject(result, new TypeReference<R<?>>() {
        }));
    }

    public Mono<R<?>> deleteUserVolume(DeleteVolumeReq deleteVolumeReq, String traceId) {
        String deleteUserVolumeUrl = UrlBuilder.ofHttp(storageSvcBaseUrl).addPath("/storage/volume/delete").build();
        return doDeleteVolume(deleteUserVolumeUrl, deleteVolumeReq, traceId);
    }

    private Mono<R<?>> doDeleteVolume(String deleteVolumeUrl, DeleteVolumeReq deleteVolumeReq, String traceId) {
        return post(deleteVolumeUrl, traceId, null,
                MapBuilder.create(new HashMap<String, Object>())
                        .put("volumeName", deleteVolumeReq.getVolumeName())
                        .put("tid", deleteVolumeReq.getTid())
                        .build()).map(result -> JSON.parseObject(result, new TypeReference<R<?>>() {
        }));
    }

    public Mono<String> mountUserVolume() {
        return Mono.empty();
    }
}
