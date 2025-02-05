package org.yx.hoststack.edge.client;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.config.EdgeClientConfig;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Component
public class EdgeClient implements Runnable {
    private final String upWsAddr;
    private EventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;
    private final EdgeClientConfig edgeClientConfig;
    private final EdgeClientChannelInitializer edgeClientChannelInitializer;
    private URI webSocketUri;

    public EdgeClient(@Value("${upWsAddr}") String upWsAddr,
                      EdgeClientConfig edgeClientConfig,
                      EdgeClientChannelInitializer edgeClientChannelInitializer) {
        this.upWsAddr = upWsAddr;
        this.edgeClientConfig = edgeClientConfig;
        this.edgeClientChannelInitializer = edgeClientChannelInitializer;
        try {
            webSocketUri = new URI(upWsAddr);
        } catch (URISyntaxException e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.EdgeWsClient_InitError)
                    .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                    .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                    .p(HostStackConstants.REGION, EdgeContext.Region)
                    .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                    .p("TargetUrl", upWsAddr)
                    .e(e);
        }
    }

    private EventLoopGroup buildEventLoopGroup() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNamePrefix("edge-client-%d").build();
        return Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup(threadFactory);
    }

    public void start() {
        try (ForkJoinPool forkJoinPool = ForkJoinPool.commonPool()) {
            forkJoinPool.execute(this);
        }
    }

    @Override
    public void run() {
        try {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.EdgeWsClient_StartInit)
                    .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                    .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                    .p(HostStackConstants.REGION, EdgeContext.Region)
                    .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                    .p("TargetUrl", upWsAddr)
                    .i();
            init();
        } catch (Exception ex) {
            destroy();
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.EdgeWsClient_InitError)
                    .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                    .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                    .p(HostStackConstants.REGION, EdgeContext.Region)
                    .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                    .p("TargetUrl", upWsAddr)
                    .e(ex);
            System.exit(0);
        }
    }

    private synchronized void init() {
        if (bootstrap == null) {
            eventLoopGroup = buildEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_RCVBUF, edgeClientConfig.getRecBuf())
                    .option(ChannelOption.SO_SNDBUF, edgeClientConfig.getSendBuf())
                    .handler(edgeClientChannelInitializer);
        }
        try {
            ChannelFuture channelFuture = bootstrap.connect(webSocketUri.getHost(), webSocketUri.getPort()).sync();
            ClientWaitConnectSignal.reset();
            boolean result = ClientWaitConnectSignal.await(edgeClientConfig.getConnectTimeout(), TimeUnit.SECONDS);
            if (result && channelFuture.channel().isActive() && channelFuture.channel().isOpen()) {
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.EdgeWsClient_ConnectSuccessful)
                        .p(HostStackConstants.CHANNEL_ID, channelFuture.channel().id())
                        .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                        .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                        .p(HostStackConstants.REGION, EdgeContext.Region)
                        .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                        .p("TargetUrl", upWsAddr)
                        .i();
                EdgeClientConnector.getInstance().create(channelFuture);
                sendEdgeRegister(channelFuture);
            } else {
                if (channelFuture.channel().isActive() || channelFuture.channel().isOpen()) {
                    channelFuture.channel().close();
                } else {
                    ClientWaitConnectSignal.release();
                    reConnect();
                }
            }
        } catch (Exception ex) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.EdgeWsClient_ConnectError)
                    .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                    .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                    .p(HostStackConstants.REGION, EdgeContext.Region)
                    .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                    .p("TargetUrl", upWsAddr)
                    .e(ex);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {

            }
            reConnect();
        } finally {
            ClientWaitConnectSignal.release();
        }
    }

    public void reConnect() {
        init();
    }

    private void sendEdgeRegister(ChannelFuture channelFuture) {
        channelFuture.addListener((ChannelFutureListener) future -> {
            Channel channel = future.channel();
            try {
                EdgeClientConnector.getInstance().edgeRegister();
            } catch (Exception ex) {
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.SendMsgFailed)
                        .p(HostStackConstants.CHANNEL_ID, channel.id())
                        .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                        .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                        .p(HostStackConstants.REGION, EdgeContext.Region)
                        .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                        .p("TargetUrl", upWsAddr)
                        .e(ex);
            }
        });
    }

    @PreDestroy
    public void destroy() {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.EdgeWsClient_PrepareDestroy)
                .i();
        EdgeClientConnector.getInstance().close();
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
        }
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.EdgeWsClient_DestroySuccessful)
                .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                .p(HostStackConstants.REGION, EdgeContext.Region)
                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                .p("TargetUrl", upWsAddr)
                .i();
    }
}
