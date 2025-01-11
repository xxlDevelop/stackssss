package org.yx.hoststack.edge.client;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.thread.ThreadUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.edge.client.jobrenotify.JobReNotifyService;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.config.EdgeClientConfig;
import org.yx.hoststack.edge.config.EdgeCommonConfig;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.SpringContextHolder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class EdgeClient implements Runnable {
    private final String upWsAddr;
    private EventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;
    private final EdgeClientConfig edgeClientConfig;
    private final EdgeClientChannelInitializer edgeClientChannelInitializer;
    private final ScheduledExecutorService reSendJobNotifyScheduler;
    private URI webSocketUri;
    private AtomicBoolean isShundown = new AtomicBoolean(false);
    private ChannelFuture channelFuture = null;

    public EdgeClient(@Value("${upWsAddr}") String upWsAddr,
                      EdgeClientConfig edgeClientConfig,
                      EdgeClientChannelInitializer edgeClientChannelInitializer,
                      JobReNotifyService jobReNotifyService,
                      EdgeCommonConfig edgeCommonConfig) {
        this.upWsAddr = upWsAddr;
        this.edgeClientConfig = edgeClientConfig;
        this.edgeClientChannelInitializer = edgeClientChannelInitializer;
        try {
            webSocketUri = new URI(upWsAddr);
        } catch (URISyntaxException e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.INIT_ERROR)
                    .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                    .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                    .p(HostStackConstants.REGION, EdgeContext.Region)
                    .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                    .p("TargetUrl", upWsAddr)
                    .e(e);
        }
        reSendJobNotifyScheduler = Executors.newScheduledThreadPool(1,
                ThreadFactoryBuilder.create().setNamePrefix("resend-job-notify").build());
        reSendJobNotifyScheduler.scheduleAtFixedRate(jobReNotifyService::reSendJobNotify, 10, 10, TimeUnit.SECONDS);
    }

    private EventLoopGroup buildEventLoopGroup() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNamePrefix("edge-client-%d").build();
        return Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup(threadFactory);
    }

    public void start() {
        ThreadUtil.execute(this);
    }

    @Override
    public void run() {
        try {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.START_INIT)
                    .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                    .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                    .p(HostStackConstants.REGION, EdgeContext.Region)
                    .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                    .p("TargetUrl", upWsAddr)
                    .i();
            connectToServer();
        } catch (Exception ex) {
            destroy();
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.INIT_ERROR)
                    .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                    .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                    .p(HostStackConstants.REGION, EdgeContext.Region)
                    .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                    .p("TargetUrl", upWsAddr)
                    .e(ex);
            System.exit(0);
        }
    }

    private synchronized void connectToServer() {
        if (isShundown.get()) {
            return;
        }
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
        EdgeClientConnector edgeClientConnector = EdgeClientConnector.getInstance();
        AtomicBoolean isConnecting = new AtomicBoolean(false);
        for (; ; ) {
            if (isShundown.get()) {
                break;
            }
            if (isConnecting.get()) {
                return;
            }
            isConnecting.set(true);
            try {
                ClientWaitConnectSignal.reset();
                edgeClientConnector.disConnect();
                channelFuture = bootstrap.connect(webSocketUri.getHost(), webSocketUri.getPort()).sync();
                ClientWaitConnectSignal.await(3, TimeUnit.SECONDS);
                channelFuture.addListener(future -> {
                    isConnecting.set(false);
                    if (future.isSuccess()) {
                        if (channelFuture.channel().isActive() && channelFuture.channel().isOpen() && channelFuture.channel().isWritable()) {
                            KvLogger.instance(this)
                                    .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.CONNECT_SUCCESSFUL)
                                    .p(HostStackConstants.CHANNEL_ID, channelFuture.channel().id())
                                    .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                                    .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                                    .p(HostStackConstants.REGION, EdgeContext.Region)
                                    .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                                    .p("TargetUrl", upWsAddr)
                                    .i();
                            edgeClientConnector.create(channelFuture);
                            sendEdgeRegister();
                        }
                    }
                });
                channelFuture.channel().closeFuture().sync();
            } catch (Exception ex) {
                KvLogger kvLogger = KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.CONNECT_ERROR)
                        .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                        .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                        .p(HostStackConstants.REGION, EdgeContext.Region)
                        .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                        .p("TargetUrl", upWsAddr);
                kvLogger.w();
                kvLogger.e(ex);
            } finally {
                ClientWaitConnectSignal.release();
                isConnecting.set(false);
            }
            ThreadUtil.sleep(3000);
        }
    }

    private void sendEdgeRegister() {
        try {
            EdgeClientConnector.getInstance().edgeRegister();
        } catch (Exception ex) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.SEND_MSG_FAILED)
                    .p(HostStackConstants.CHANNEL_ID, channelFuture.channel().id())
                    .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                    .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                    .p(HostStackConstants.REGION, EdgeContext.Region)
                    .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                    .p("TargetUrl", upWsAddr)
                    .e(ex);
        }
    }

    public void destroy() {
        isShundown.set(true);
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.PREPARE_DESTROY)
                .i();
        EdgeClientConnector.getInstance().destroy();
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
        }
        if (reSendJobNotifyScheduler != null && !reSendJobNotifyScheduler.isShutdown()) {
            reSendJobNotifyScheduler.shutdown();
        }
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.DESTROY_SUCCESSFUL)
                .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                .p(HostStackConstants.REGION, EdgeContext.Region)
                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                .p("TargetUrl", upWsAddr)
                .i();
    }
}
