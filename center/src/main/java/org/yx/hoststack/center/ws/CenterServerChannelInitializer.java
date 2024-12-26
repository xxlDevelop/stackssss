package org.yx.hoststack.center.ws;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yx.hoststack.center.ws.config.CenterServerConfig;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;

@Component
@RequiredArgsConstructor
public class CenterServerChannelInitializer extends ChannelInitializer<Channel> {
    private final CenterServerConfig edgeServerConfig;

    private final WebSocketServerHandler webSocketServerHandler;

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline p = channel.pipeline();
        if (edgeServerConfig.isEnableIdle()) {
            p.addLast().addLast(new IdleStateHandler(edgeServerConfig.getReadIdle(), edgeServerConfig.getWriteIdle(), edgeServerConfig.getAllIdle()));
        }
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpObjectAggregator(65536));
        p.addLast(new ChunkedWriteHandler());
        p.addLast(new WebSocketServerProtocolHandler("/"));
        p.addLast(new WebSocketServerCompressionHandler());
        p.addLast(new ProtobufDecoder(CommonMessageWrapper.CommonMessage.getDefaultInstance()));
        p.addLast(new ProtobufEncoder());
        p.addLast(webSocketServerHandler);
    }
}
