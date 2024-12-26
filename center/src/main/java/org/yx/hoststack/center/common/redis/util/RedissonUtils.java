package org.yx.hoststack.center.common.redis.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.LocalCachedMapOptions;
import org.redisson.api.options.LocalCachedMapParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description : Redisson utility class
 * @Author : Lee666
 * @Date : 2024/1/4
 * @Version : 1.0
 */
@RefreshScope
@Component
@Slf4j
public class RedissonUtils {

    @Value("${applications.redisson.timeToLive}")
    private Long timeToLive;
    @Value("${applications.redisson.maxIdle}")
    private Long maxIdle;

    private static final ConcurrentHashMap<String, RLocalCachedMap<?, ?>> localCacheMap = new ConcurrentHashMap<>();

    private final RedissonClient redissonClientBean;
    public static RedissonClient redissonClient;
    private static volatile Long maxIdleStatic;
    private static volatile Long timeToLiveStatic;
    ;

    public RedissonUtils(RedissonClient redissonClientBean) {
        this.redissonClientBean = redissonClientBean;
    }

    @PostConstruct
    public void init() {
        log.debug("msg:'Init redissonClient'");
        RedissonUtils.redissonClient = redissonClientBean;
        refreshLocalCachedMapOptions();
    }

    /**
     * get common local cached map options
     *
     * @param name       redis key
     * @param timeToLive ttl; redis and local cache ttl
     * @param maxIdle    max idle; redis and local cache ttl
     * @param <K>        key
     * @param <V>        data
     * @return options
     */
    public static <K, V> LocalCachedMapOptions<K, V> getLocalCachedMapOptions(String name, Long timeToLive, Long maxIdle) {
        return LocalCachedMapOptions.<K, V>name(name)
                // If cache size is 0 then local cache is unbounded.
                .cacheSize(0)
                // Defines local cache eviction policy.
                // Follow options are available:
                // LFU - Counts how often an item was requested. Those that are used least often are discarded first.
                // LRU - Discards the least recently used items first
                // SOFT - Uses soft references, entries are removed by GC
                // WEAK - Uses weak references, entries are removed by GC
                // NONE - No eviction
                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LFU)
                .timeToLive(timeToLive != null ? Duration.of(timeToLive, ChronoUnit.MILLIS) : Duration.of(timeToLiveStatic, ChronoUnit.MILLIS))
                .maxIdle(maxIdle != null ? Duration.of(maxIdle, ChronoUnit.MILLIS) : Duration.of(maxIdleStatic, ChronoUnit.MILLIS))
                // Defines whether to store a cache miss into the local cache.
                // Default value is false.
                .storeCacheMiss(false)
                // Defines store mode of cache data.
                // Follow options are available:
                // LOCALCACHE - store data in local cache only and use Redis or Valkey only for data update/invalidation.
                // LOCALCACHE_REDIS - store data in both Redis or Valkey and local cache.
                .storeMode(LocalCachedMapOptions.StoreMode.LOCALCACHE_REDIS)
                // Defines Cache provider used as local cache store.
                // Follow options are available:
                // REDISSON - uses Redisson own implementation
                // CAFFEINE - uses Caffeine implementation
                .cacheProvider(LocalCachedMapOptions.CacheProvider.REDISSON)
                // Defines strategy for load missed local cache updates after connection failure.
                //
                // Follow reconnection strategies are available:
                // CLEAR - Clear local cache if map instance has been disconnected for a while.
                // NONE - Default. No reconnection handling
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.NONE)
                // Defines local cache synchronization strategy.
                //
                // Follow sync strategies are available:
                // INVALIDATE - Default. Invalidate cache entry across all RLocalCachedJsonStore instances on map entry change
                // UPDATE - Insert/update cache entry across all RLocalCachedJsonStore instances on map entry change
                // NONE - No synchronizations on map changes
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.INVALIDATE)
                // Defines how to listen expired event sent by Redis or Valkey upon this instance deletion
                //
                // Follow expiration policies are available:
                // DONT_SUBSCRIBE - Don't subscribe on expire event
                // SUBSCRIBE_WITH_KEYEVENT_PATTERN - Subscribe on expire event using `__keyevent@*:expired` pattern
                // SUBSCRIBE_WITH_KEYSPACE_CHANNEL - Subscribe on expire event using `__keyspace@N__:name` channel
                .expirationEventPolicy(LocalCachedMapOptions.ExpirationEventPolicy.SUBSCRIBE_WITH_KEYEVENT_PATTERN);
    }

    /**
     * Refresh local cache options
     */
    public synchronized void refreshLocalCachedMapOptions() {
        log.debug("Action:'refreshLocalCachedMapOptions' msg:'Refreshing local cached map options'");
        RedissonUtils.maxIdleStatic = maxIdle;
        RedissonUtils.timeToLiveStatic = timeToLive;
    }

    /**
     * Set String cache
     */
    public static <T> void setStr(String key, T value) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }


    /**
     * Set String cache with expiration time
     */
    public static <T> void setStr(String key, T value, Duration ttl) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value, ttl);
    }

    /**
     * Get String cache
     */
    public static <T> T getStr(String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        if (bucket == null) {
            return null;
        }
        return bucket.get();
    }

    /**
     * Write to local cache and synchronize Redis and all local caches with the same name
     *
     * @param key        cache key
     * @param data       cache data
     * @param timeToLive ttl
     * @param maxIdle    local ttl
     * @param <K>        data Key item
     * @param <V>        data value item
     */
    public static <K, V> void setLocalCachedMap(String key, String subKey, V data, Long timeToLive, Long maxIdle) {
        setLocalCachedMap(key, subKey, data, getLocalCachedMapOptions(key, timeToLive, maxIdle));
    }

    /**
     * Write to local cache
     * The name of options is the key of Redis, which is required
     */
    public static <K, V> void setLocalCachedMap(String key, K subKey, V data, LocalCachedMapOptions<K, V> options) {
        LocalCachedMapParams<K, V> params = (LocalCachedMapParams<K, V>) options;
        Map<K, V> cache = new ConcurrentHashMap<>(1);
        cache.put(subKey, data);
        RLocalCachedMap<K, V> localCachedMap = (RLocalCachedMap<K, V>) localCacheMap.computeIfAbsent(key, k -> redissonClient.getLocalCachedMap(params));
        localCachedMap.putAll(cache);
        localCachedMap.expire(Duration.ofMillis(params.getTimeToLiveInMillis()));
        log.debug("Action:'setLocalCachedMap' msg:'Loading data into cache -> {}'", localCachedMap.getCachedMap());
    }

    /**
     * Write to local cache
     * The name of options is the key of Redis, which is required
     */
    public static <K, V> void setLocalCachedMap(Map<K, V> data, LocalCachedMapOptions<K, V> options) {
        LocalCachedMapParams<K, V> params = (LocalCachedMapParams<K, V>) options;
        setLocalCachedMap(data, options, Duration.ofMillis(params.getTimeToLiveInMillis()));
    }

    /**
     * Write to local cache
     * The name of options is the key of Redis, which is required
     */
    public static <K, V> void setLocalCachedMap(Map<K, V> data, LocalCachedMapOptions<K, V> options, Duration redisTtl) {
        LocalCachedMapParams<K, V> params = (LocalCachedMapParams<K, V>) options;
        RLocalCachedMap<K, V> localCachedMap = (RLocalCachedMap<K, V>) localCacheMap.computeIfAbsent(params.getName(), k -> redissonClient.getLocalCachedMap(params));
        localCachedMap.putAll(data);
        if (redisTtl != null) {
            localCachedMap.expire(redisTtl);
        }
        log.debug("Action:'setLocalCachedMap' Msg:'Loading data into cache -> {}'", localCachedMap.getCachedMap());
    }

    /**
     * Get local cache
     */
    public static <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String key) {
        RLocalCachedMap<K, V> aLocalCache = (RLocalCachedMap<K, V>) localCacheMap.get(key);
        if (aLocalCache == null) {
            LocalCachedMapParams<K, V> params = (LocalCachedMapParams<K, V>) getLocalCachedMapOptions(key, null, null);
            aLocalCache = (RLocalCachedMap<K, V>) localCacheMap.computeIfAbsent(params.getName(), k -> redissonClient.getLocalCachedMap(params));
        }
        return aLocalCache;
    }

    /**
     * Get local cache value
     */
    public static <K, V> V getLocalCache(String key, K subKey) {
        LocalCachedMapParams<K, V> params = (LocalCachedMapParams<K, V>) getLocalCachedMapOptions(key, null, null);
        RLocalCachedMap<K, V> aLocalCache = (RLocalCachedMap<K, V>) localCacheMap.computeIfAbsent(params.getName(), k -> redissonClient.getLocalCachedMap(params));
        return aLocalCache.get(subKey);
    }

    /**
     * Get local cache
     */
    public static <K, V> V delLocalCachedMap(String key, String subKey) {
        RLocalCachedMap<K, V> aLocalCache = (RLocalCachedMap<K, V>) localCacheMap.get(key);
        if (aLocalCache == null) {
            return null;
        }
        aLocalCache = redissonClient.getLocalCachedMap(getLocalCachedMapOptions(key, null, null));
        return aLocalCache.remove(subKey);
    }

    /**
     * Get local cache
     */
    public static RLock getLock(String key) {
        return redissonClient.getLock(key);
    }

    /**
     * Set cache for string type and specify TTL (in seconds)
     *
     * @param key   key
     * @param value cache data
     * @param ttl   Survival time (seconds)
     */
    public static void setString(String key, String value, int ttl) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        if (ttl > 0) {
            bucket.set(value, Duration.ofSeconds(ttl));
        } else {
            bucket.set(value);
        }
    }

    /**
     * Retrieve the cache of the string type for the specified key
     *
     * @param key key
     * @return Cache value or null (if not present)
     */
    public static String getString(String key) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     * del str cache
     *
     * @param key key
     */
    public static boolean deleteString(String key) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.delete();
    }
}
