package org.yx.hoststack.edge.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;

import java.net.URI;
import java.net.URISyntaxException;

@Component
@RequiredArgsConstructor
public class EdgeClientChannelInitializer extends ChannelInitializer<Channel> {
    @Value("${upWsAddr}")
    private String upWsAddr;

    private final EdgeClientMsgHandler edgeClientMsgHandler;

    @Override
    protected void initChannel(Channel channel) throws URISyntaxException {
        ChannelPipeline p = channel.pipeline();
        p.addLast(new HttpClientCodec());
        p.addLast(new HttpObjectAggregator(8192));
        p.addLast(new WebSocketClientProtocolHandler(WebSocketClientHandshakerFactory
                .newHandshaker(new URI(upWsAddr), WebSocketVersion.V13, null, false, new DefaultHttpHeaders())));
        p.addLast(new ProtobufDecoder(CommonMessageWrapper.CommonMessage.getDefaultInstance()));
        p.addLast(new ProtobufEncoder());
        p.addLast(edgeClientMsgHandler);
    }
}
