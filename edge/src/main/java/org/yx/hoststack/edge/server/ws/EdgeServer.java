package org.yx.hoststack.edge.server.ws;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.google.protobuf.ByteString;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.edge.client.EdgeClientConnector;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.config.EdgeServerConfig;
import org.yx.hoststack.edge.server.ws.session.SessionManager;
import org.yx.hoststack.edge.server.ws.session.SessionReSendMap;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class EdgeServer implements Runnable {
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors() * 3;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final EdgeServerConfig edgeServerConfig;

    private final EdgeServerChannelInitializer edgeServerChannelInitializer;

    private final SessionManager sessionManager;

    private final ScheduledExecutorService reSendMsgScheduler = Executors.newScheduledThreadPool(1,
            ThreadFactoryBuilder.create().setNamePrefix("server-reSend-").build());

    private int bossThreadCount() {
        return edgeServerConfig.getBossThreadCount() <= 1 ? 1 : edgeServerConfig.getBossThreadCount();
    }

    private int workThreadCount() {
        return edgeServerConfig.getWorkThreadCount() <= 5 ? AVAILABLE_PROCESSORS + 1 : edgeServerConfig.getWorkThreadCount();
    }

    public EventLoopGroup buildBossGroup() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNamePrefix("edge-boss-%d").build();
        return Epoll.isAvailable() ? new EpollEventLoopGroup(bossThreadCount(), threadFactory) : new NioEventLoopGroup(bossThreadCount(), threadFactory);
    }

    public EventLoopGroup buildWorkerGroup() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNamePrefix("edge-worker-%d").build();
        return Epoll.isAvailable() ? new EpollEventLoopGroup(workThreadCount(), threadFactory) : new NioEventLoopGroup(workThreadCount(), threadFactory);
    }

    public InetSocketAddress inetSocketAddress() {
        return new InetSocketAddress(edgeServerConfig.getWsPort());
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
                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                    .p(LogFieldConstants.ACTION, "StartInit")
                    .i();
            this.init();
        } catch (Exception ex) {
            destroy();
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                    .p(LogFieldConstants.ACTION, "InitError")
                    .e(ex);
            System.exit(0);
        }
    }

    private void init() throws InterruptedException {
        int port = edgeServerConfig.getWsPort();
        bossGroup = this.buildBossGroup();
        workerGroup = this.buildWorkerGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .localAddress(inetSocketAddress())
                .option(ChannelOption.SO_BACKLOG, edgeServerConfig.getBacklog())
                .childOption(ChannelOption.SO_RCVBUF, edgeServerConfig.getRecBuf())
                .childOption(ChannelOption.SO_SNDBUF, edgeServerConfig.getSendBuf())
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childHandler(edgeServerChannelInitializer);

        ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
        if (channelFuture.isSuccess()) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                    .p(LogFieldConstants.ACTION, "InitSuccessfully")
                    .p("ListenerPort", port)
                    .p("BossThreadCount", bossThreadCount())
                    .p("WorkThreadCount", workThreadCount())
                    .p("SelectPoolMode", (Epoll.isAvailable() ? "Epoll" : "Nio"))
                    .i();
//            // monitor
//            startServerMonitor();
            // retry send
            startRetrySend(channelFuture);
            // block
            channelFuture.channel().closeFuture().sync();
        } else {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                    .p(LogFieldConstants.ACTION, "ChannelRegisterFailed")
                    .i();
            destroy();
        }
    }

    /**
     * start retry send
     *
     * @param channelFuture channelFuture
     */
    private void startRetrySend(ChannelFuture channelFuture) {
        channelFuture.addListener((ChannelFutureListener) future -> reSendMsgScheduler.scheduleAtFixedRate(() -> {
            if (SessionReSendMap.getData().mappingCount() > 0) {
                SessionReSendMap.getData().forEach((resendMessageId, resendMessage) -> {
                    Channel resendChannel = resendMessage.getChannel();
                    AgentCommonMessage<?> reSendAgentCommonMessage = resendMessage.getData().getMessage();
                    try {
                        AtomicInteger retry = new AtomicInteger(resendMessage.getRetry());
                        KvLogger kvLogger = KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                                .p(HostStackConstants.CHANNEL_ID, resendChannel.id())
                                .p(HostStackConstants.METH_ID, reSendAgentCommonMessage.getMethod())
                                .p(HostStackConstants.TRACE_ID, reSendAgentCommonMessage.getTraceId())
                                .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                                .p(HostStackConstants.REGION, EdgeContext.Region)
                                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                                .p("ReSendId", resendMessage.getReSendId())
                                .p("RetryTimes", retry.get());
                        if (!resendChannel.isActive() || !resendChannel.isOpen() || !resendChannel.isWritable()) {
                            SessionReSendMap.remove(resendMessageId);
                            kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.ReSendMsgFailed)
                                    .p(LogFieldConstants.ERR_MSG, "Channel is not alive")
                                    .w();
                            EdgeClientConnector.getInstance().sendResultToUpstream(resendMessage.getData().getCenterMethId(),
                                    EdgeSysCode.SendAgentFailByChannelNotActive.getValue(), EdgeSysCode.SendAgentFailByChannelNotActive.getMsg(),
                                    ByteString.EMPTY, reSendAgentCommonMessage.getTraceId());
                            return;
                        }
                        if (retry.get() >= edgeServerConfig.getRetryNumber()) {
                            SessionReSendMap.remove(resendMessageId);
                            KvLogger.instance(this)
                                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.ReSendMsgFailedLimit)
                                    .p(HostStackConstants.CHANNEL_ID, resendChannel.id())
                                    .p(HostStackConstants.METH_ID, reSendAgentCommonMessage.getMethod())
                                    .p(HostStackConstants.TRACE_ID, reSendAgentCommonMessage.getTraceId())
                                    .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                                    .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                                    .p(HostStackConstants.REGION, EdgeContext.Region)
                                    .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                                    .p("ReSendId", resendMessage.getReSendId())
                                    .p("RetryTimes", retry.get())
                                    .w();
                            EdgeClientConnector.getInstance().sendResultToUpstream(resendMessage.getData().getCenterMethId(),
                                    EdgeSysCode.SendAgentFailByLimit.getValue(), EdgeSysCode.SendAgentFailByLimit.getMsg(), ByteString.EMPTY, reSendAgentCommonMessage.getTraceId());
                            return;
                        }
//                            resendChannel.eventLoop().execute(() -> {
                        ChannelFuture reSendChannelFuture = resendChannel.writeAndFlush(new TextWebSocketFrame(reSendAgentCommonMessage.toString()));
                        resendMessage.setRetry(retry.incrementAndGet());
                        reSendChannelFuture.addListener(retryFuture -> {
                            if (retryFuture.isDone() && retryFuture.cause() != null) {
                                kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.ReSendMsgFailed)
                                        .p(LogFieldConstants.ERR_MSG, retryFuture.cause().getMessage())
                                        .p(LogFieldConstants.ReqData, reSendAgentCommonMessage.toString())
                                        .e(retryFuture.cause());
                            } else if (retryFuture.isDone() && retryFuture.isSuccess()) {
                                SessionReSendMap.remove(resendMessageId);
                                kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.ReSendMsgSuccessful)
                                        .i();
                                kvLogger.p(LogFieldConstants.ReqData, reSendAgentCommonMessage.toString())
                                        .d();
                            }
                        });
//                            });
                    } catch (Exception ex) {
                        KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                                .p(LogFieldConstants.ACTION, EdgeEvent.Action.ReSendMsgFailed)
                                .p(HostStackConstants.CHANNEL_ID, resendChannel.id())
                                .e(ex);
                    }
                });
            }
        }, 10, 10, TimeUnit.SECONDS));
    }

    public void destroy() {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                .p(LogFieldConstants.ACTION, "PrepareDestroy")
                .i();
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (reSendMsgScheduler != null && !reSendMsgScheduler.isShutdown()) {
            reSendMsgScheduler.shutdown();
        }
        sessionManager.destroy();
        SessionReSendMap.clear();
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                .p(LogFieldConstants.ACTION, "DestroySuccessfully")
                .i();
    }
}
