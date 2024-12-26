package org.yx.hoststack.center.ws.common;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    // 使用 ConcurrentHashMap 存储 serviceIp -> Channel 映射，保证线程安全
    public static final ConcurrentHashMap<String, Channel> connections = new ConcurrentHashMap<>();

    // 添加连接
    public static void addConnection(String hostId, Channel channel) {
        connections.put(hostId, channel);
    }

    // 移除连接
    public static void removeConnection(String hostId) {
        connections.remove(hostId);
    }

    // 获取连接
    public static Channel getConnection(String hostId) {
        return connections.get(hostId);
    }

    // 检查连接是否存在
    public static boolean containsConnection(String hostId) {
        return connections.containsKey(hostId);
    }
}
