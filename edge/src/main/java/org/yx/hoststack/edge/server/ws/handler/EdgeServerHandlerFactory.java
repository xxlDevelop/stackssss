package org.yx.hoststack.edge.server.ws.handler;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EdgeServerHandlerFactory {
    private final EdgeServerHandshakeProcessHandler edgeServerHandshakeProcessHandler;
    private final EdgeServerMsgProcessHandler edgeServerMsgProcessHandler;

    public Optional<ChannelHandler> getHandler(Object msg) {
        if (msg instanceof FullHttpRequest) {
            return Optional.of(edgeServerHandshakeProcessHandler);
        }
        if (msg instanceof WebSocketFrame) {
            return Optional.of(edgeServerMsgProcessHandler);
        }
        return Optional.empty();
    }
}

