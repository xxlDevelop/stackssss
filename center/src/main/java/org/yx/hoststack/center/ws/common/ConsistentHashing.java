package org.yx.hoststack.center.ws.common;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 一致性哈希实现
 */
public class ConsistentHashing<T> {

    private final SortedMap<Integer, T> ring = new TreeMap<>();
    private final int numReplicas;

    /**
     * 构造函数
     *
     * @param numReplicas 每个服务分片的虚拟副本数量
     */
    public ConsistentHashing(int numReplicas) {
        this.numReplicas = numReplicas;
    }

    /**
     * 添加服务分片到哈希环
     *
     * @param shard 服务分片对象
     */
    public void addShard(T shard) {
        for (int i = 0; i < numReplicas; i++) {
            int hash = (shard.toString() + i).hashCode();
            ring.put(hash, shard);
        }
    }

    /**
     * 从哈希环中移除服务分片
     *
     * @param shard 服务分片对象
     */
    public void removeShard(T shard) {
        for (int i = 0; i < numReplicas; i++) {
            int hash = (shard.toString() + i).hashCode();
            ring.remove(hash);
        }
    }

    /**
     * 根据 key 获取对应的服务分片
     *
     * @param key 要查找的 key
     * @return 对应的服务分片对象
     */
    public T getShard(String key) {
        if (ring.isEmpty()) {
            return null;
        }

        int hash = key.hashCode();

        // 查找第一个大于等于哈希值的服务分片
        if (!ring.containsKey(hash)) {
            SortedMap<Integer, T> tailMap = ring.tailMap(hash);
            hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
        }

        return ring.get(hash);
    }

    private static class Shard {
        private final String id;

        public Shard(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return id;
        }
    }
}
