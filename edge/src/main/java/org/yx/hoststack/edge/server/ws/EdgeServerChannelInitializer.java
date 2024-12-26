package org.yx.hoststack.edge.server.ws;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.config.EdgeServerConfig;
import org.yx.hoststack.edge.server.ws.handler.EdgeServerProcessHandler;

@Component
@RequiredArgsConstructor
public class EdgeServerChannelInitializer extends ChannelInitializer<Channel> {
    private final EdgeServerConfig edgeServerConfig;

    private final EdgeServerProcessHandler edgeServerProcessHandler;

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline p = channel.pipeline();
        if (edgeServerConfig.isEnableIdle()) {
            p.addLast().addLast(new IdleStateHandler(edgeServerConfig.getReadIdle(), edgeServerConfig.getWriteIdle(), edgeServerConfig.getAllIdle()));
        }
        p.addLast(new HttpServerCodec());
        p.addLast(new ChunkedWriteHandler());
        p.addLast(new HttpObjectAggregator(8192));
        p.addLast(new WebSocketServerCompressionHandler());
        p.addLast(edgeServerProcessHandler);
    }
}
