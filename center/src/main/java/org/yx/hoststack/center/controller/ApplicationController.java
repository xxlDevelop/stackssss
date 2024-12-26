package org.yx.hoststack.center.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.util.AppStartTimeHolder;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * <p>
 * Basic information table - bare metal container information front-end controller
 * </p>
 *
 * @author Lee
 * @since 2024-01-12
 */
@Slf4j
@Controller
@RestController
@RequestMapping("/ops")
public class ApplicationController {

    private final AppStartTimeHolder appStartTimeHolder;
    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    @Value("${app-version}")
    private String version;

    private static final String VERSION_KEY = "version";
    private static final String START_AT_KEY = "startAt";
    private static final String RUNTIMES_KEY = "runtimes";
    private static final String HEALTH_KEY = "health";
    private static final String DETAIL_KEY = "detail";
    private static final String DETAIL_ITEM_POINT_KEY = "point";
    private static final String DETAIL_ITEM_STATUS_KEY = "status";
    private static final String DETAIL_ITEM_DESCRIBE_KEY = "describe";

    private static final String MYSQL_CONNECTION_TIMEOUT = "MySQL connection timeout";

    private static final String MYSQL_DB_IDX = "IdxMysql";
    private static final String MYSQL_DB_DS_IDX = "idx";

    private static final String DB_REDIS = "Redis";

    private static final String MYSQL_TEST_HEALTH_SQL = "SELECT 1;";

    public ApplicationController(AppStartTimeHolder appStartTimeHolder, JdbcTemplate jdbcTemplate, StringRedisTemplate redisTemplate) {
        this.appStartTimeHolder = appStartTimeHolder;
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Health check interface
     */
    @GetMapping(value = "/health")
    public JSONObject health() {
        JSONObject result = new JSONObject();
        JSONArray detail = new JSONArray();

        result.set(VERSION_KEY, version);
        result.set(START_AT_KEY, appStartTimeHolder.getStartTime());
        result.set(RUNTIMES_KEY, System.currentTimeMillis() / 1000L - appStartTimeHolder.getStartTime());
        detail.add(checkRedis());
        detail.add(checkMysql());
        result.set(DETAIL_KEY, detail);
        Set<Integer> statusList = new HashSet<>();
        for (int i = 0; i < detail.size(); i++) {
            JSONObject detailObject = detail.getJSONObject(i);
            int status = detailObject.getInt(DETAIL_ITEM_STATUS_KEY);
            statusList.add(status);
        }
        int healthStatus = statusList.contains(NumberUtils.INTEGER_TWO) && statusList.size() > 1 ? 1 : 0;
        result.set(HEALTH_KEY, (statusList.contains(NumberUtils.INTEGER_TWO) && statusList.size() == 1) ? 2 : healthStatus);
        return result;
    }

    private JSONObject checkMysql() {
        JSONObject mysql = new JSONObject();
        mysql.set(DETAIL_ITEM_POINT_KEY, MYSQL_DB_IDX);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                DynamicDataSourceContextHolder.push(MYSQL_DB_DS_IDX);
                jdbcTemplate.queryForObject(MYSQL_TEST_HEALTH_SQL, Integer.class);
            });
            future.get(1000, TimeUnit.MILLISECONDS);
            mysql.set(DETAIL_ITEM_STATUS_KEY, NumberUtils.INTEGER_ZERO);
        } catch (TimeoutException e) {
            // Catch timeout exception and set MySQL unavailable status
            mysql.set(DETAIL_ITEM_STATUS_KEY, NumberUtils.INTEGER_TWO);
            mysql.set(DETAIL_ITEM_DESCRIBE_KEY, MYSQL_CONNECTION_TIMEOUT);
        } catch (Exception e) {
            mysql.set(DETAIL_ITEM_STATUS_KEY, NumberUtils.INTEGER_TWO);
            mysql.set(DETAIL_ITEM_DESCRIBE_KEY, e.getMessage());
        } finally {
            shutdownExecutor(executor);
        }

        return mysql;
    }

    private JSONObject checkRedis() {
        JSONObject redis = new JSONObject();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            redis.set(DETAIL_ITEM_POINT_KEY, DB_REDIS);
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        redisTemplate.opsForValue().get("test");
                        redisTemplate.opsForValue().set("test", HEALTH_KEY);
                    }
            );
            future.get(500, TimeUnit.MILLISECONDS);
            redis.set(DETAIL_ITEM_STATUS_KEY, NumberUtils.INTEGER_ZERO);
        } catch (Exception e) {
            redis.set(DETAIL_ITEM_STATUS_KEY, NumberUtils.INTEGER_TWO);
            redis.set(DETAIL_ITEM_DESCRIBE_KEY, e.getMessage());
        } finally {
            shutdownExecutor(executor);
        }
        return redis;
    }

    private void shutdownExecutor(ExecutorService executor) {
        KvLogger.instance(this)
                .p("Msg", "Send interrupt signals to all ongoing tasks")
                .i();
        executor.shutdownNow();

        try {
            KvLogger.instance(this)
                    .p("Msg", "Wait for all tasks to complete, up to a maximum of 10 seconds")
                    .i();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                KvLogger.instance(this)
                        .p("Msg", "If there are still unfinished tasks after 10 seconds, send the interrupt signal again")
                        .i();
                executor.shutdownNow();
                KvLogger.instance(this)
                        .p("Msg", "Executor did not terminate after the first shutdownNow call.")
                        .w();
            }
        } catch (InterruptedException e) {
            KvLogger.instance(this)
                    .p("Msg", "Restore interrupted state")
                    .w();
            Thread.currentThread().interrupt();
            KvLogger.instance(this)
                    .p("Msg", "Send the interrupt signal again")
                    .w();
            executor.shutdownNow();
            KvLogger.instance(this)
                    .p("Msg", "Interrupted while waiting for executor to terminate.")
                    .e(e);
        }
    }
}
