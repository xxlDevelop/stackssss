package org.yx.hoststack.edge.server.http.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.yx.lib.utils.util.AppStartTimeHolder;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class HealthHandler {
    private final RedissonClient redissonClient;
    private final AppStartTimeHolder appStartTimeHolder;

    public Mono<ServerResponse> health(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.fromSupplier(() -> {
                    JSONObject result = new JSONObject();
                    JSONArray detail = new JSONArray();
                    String gitVersion = "unKnow";
                    String buildTime = "unKnow";
                    String gitBranch = "unKnow";
                    try {
                        Resource resource = new ClassPathResource("git.properties");
                        Properties properties = new Properties();
                        try (InputStream inputStream = resource.getInputStream()) {
                            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                                properties.load(inputStreamReader);
                                gitVersion = properties.getProperty("git.commit.id.abbrev");
                                buildTime = properties.getProperty("git.build.time");
                                gitBranch = properties.getProperty("git.branch");
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    result.put("version", gitBranch + "." + gitVersion);
                    result.put("buildTime", buildTime);
                    result.put("startAt", appStartTimeHolder.getStartTime());
                    result.put("runtimes", System.currentTimeMillis() / 1000L - appStartTimeHolder.getStartTime());
                    detail.add(checkRedis());
                    result.put("detail", detail);

                    Set<Integer> statusList = new HashSet<>();
                    for (int i = 0; i < detail.size(); i++) {
                        JSONObject detailObject = detail.getJSONObject(i);
                        int status = detailObject.getInteger("status");
                        statusList.add(status);
                    }
                    result.put("health", (statusList.contains(NumberUtils.INTEGER_TWO) && statusList.size() == 1) ? 2 :
                            (statusList.contains(NumberUtils.INTEGER_TWO) && statusList.size() > 1) ? 1 : 0);
                    return result;
                }), JSONObject.class);
    }

    private JSONObject checkRedis() {
        JSONObject redis = new JSONObject();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            redis.put("point", "Redis");
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        redissonClient.getBucket("host_stack_edge_test", StringCodec.INSTANCE).set("health");
                    }
            );
            future.get(500, TimeUnit.MILLISECONDS);
            redis.put("status", NumberUtils.INTEGER_ZERO);
        } catch (Exception e) {
            redis.put("status", NumberUtils.INTEGER_TWO);
            redis.put("describe", e.getMessage());
        } finally {
            shutdownExecutor(executor);
        }
        return redis;
    }

    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
