package org.yx.hoststack.center.ws.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.yx.hoststack.center.CenterApplicationRunner;
import org.yx.hoststack.center.ws.CenterServer;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static org.yx.hoststack.center.ws.CenterServer.centerNode;
import static org.yx.hoststack.center.ws.common.Node.NODE_MAP;
import static org.yx.hoststack.center.ws.controller.EdgeController.serverDetailCacheMap;

@Component
@ChannelHandler.Sharable
public class WebSocketHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Object serviceId = ctx.channel().attr(AttributeKey.valueOf("innerServiceId")).get();
        if (!ObjectUtils.isEmpty(serviceId)) {
            Node node = findNodeByServiceId(serviceId.toString());

            if (node != null) {
                node.removeNodeRecursively(node);
            }
        }
        super.channelInactive(ctx);
    }

    // 根据 serviceId 查找节点
    private Node findNodeByServiceId(String serviceId) {
        if (ObjectUtils.isEmpty(serviceId)) return null;
        return NODE_MAP.get(serviceId);
    }

    // 删除所有节点
    private void removeAllNodes() {
        for (Node node : new ArrayList<>(NODE_MAP.values())) {
            removeNodeRecursively(node);
        }
    }

    // 递归删除节点及其所有子节点
    public void removeNodeRecursively(Node node) {
        for (Node child : new ArrayList<>(node.children)) {
            removeNodeRecursively(child);
        }
        NODE_MAP.remove(node.serviceId);

        if (node.parent != null) {
            node.parent.children.remove(node);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ctx.fireChannelRead(msg);
    }
}
