package org.yx.hoststack.center.ws.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.yx.hoststack.center.CenterApplicationRunner;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static org.yx.hoststack.center.ws.common.Node.NODE_MAP;
import static org.yx.hoststack.center.ws.controller.EdgeController.serverDetailCacheMap;

@Component
@ChannelHandler.Sharable
public class WebSocketHandler extends ChannelInboundHandlerAdapter {
    private static ConcurrentHashMap<Channel, String> CacheChanelMap = new ConcurrentHashMap<>();
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String serviceId = CacheChanelMap.get(ctx.channel());
        Node node = findNodeByServiceId(serviceId);

        if (node != null) {
            node.removeNodeRecursively(node);
            serverDetailCacheMap.remove(serviceId);
        }
        CacheChanelMap.remove(ctx.channel());
        CenterApplicationRunner.centerNode.printNodeInfo(2);
        super.channelInactive(ctx);
    }

    // 根据 serviceId 查找节点
    private Node findNodeByServiceId(String serviceId) {
        if(ObjectUtils.isEmpty(serviceId)){
            return NODE_MAP.get(serviceId);
        }
        return null;
    }

    // 删除所有节点
    private void removeAllNodes() {
        // 遍历所有节点并删除
        for (Node node : new ArrayList<>(NODE_MAP.values())) {
            removeNodeRecursively(node);
        }
    }

    // 递归删除节点及其所有子节点
    public void removeNodeRecursively(Node node) {
        // 递归删除所有子节点
        for (Node child : new ArrayList<>(node.children)) {
            removeNodeRecursively(child);  // 递归删除子节点
        }

        // 从哈希表中删除该节点
        NODE_MAP.remove(node.serviceId);

        // 如果该节点有父节点，移除它的子节点
        if (node.parent != null) {
            node.parent.children.remove(node);
        }
        CenterApplicationRunner.centerNode.printNodeInfo(2);

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof BinaryWebSocketFrame) {
            ByteBuf byteBuf = ((BinaryWebSocketFrame) msg).content();
            byte[] contentBytes = ByteBufUtil.getBytes(byteBuf);
            CommonMessageWrapper.CommonMessage commonMessage = CommonMessageWrapper.CommonMessage.parseFrom(contentBytes);
            CacheChanelMap.put(ctx.channel(), commonMessage.getHeader().getRelaySid());
            CacheChanelMap.put(ctx.channel(), commonMessage.getHeader().getIdcSid());
        }
        ctx.fireChannelRead(msg);
    }
}
