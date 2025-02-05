package org.yx.hoststack.center.ws;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.ws.config.CenterServerConfig;
import org.yx.hoststack.protocol.ws.ResendMessage;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class CenterServer implements Runnable {
    private static final int availableProcessors = Runtime.getRuntime().availableProcessors() * 3;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final CenterServerConfig edgeServerConfig;

    private final CenterServerChannelInitializer centerServerChannelInitializer;

    private int bossThreadCount() {
        return edgeServerConfig.getBossThreadCount() <= 1 ? 1 : edgeServerConfig.getBossThreadCount();
    }

    private int workThreadCount() {
        return edgeServerConfig.getWorkThreadCount() <= 5 ? availableProcessors + 1 : edgeServerConfig.getWorkThreadCount();
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
        ForkJoinPool.commonPool().execute(this);
    }

    @Override
    public void run() {
        try {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CenterWsServer)
                    .p(LogFieldConstants.ACTION, "StartInit")
                    .i();
            this.init();
        } catch (Exception ex) {
            destroy();
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CenterWsServer)
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
                .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.SO_RCVBUF, edgeServerConfig.getRecBuf())
                .childOption(ChannelOption.SO_SNDBUF, edgeServerConfig.getSendBuf())
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childHandler(centerServerChannelInitializer);
//                    .handler(new LoggingHandler(msgServerConfig.getLogLevel() == 2 ? LogLevel.INFO : LogLevel.DEBUG));

        ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
        if (channelFuture.isSuccess()) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CenterWsServer)
                    .p(LogFieldConstants.ACTION, "InitSuccessfully")
                    .p("ListenerPort", port)
                    .p("BossThreadCount", bossThreadCount())
                    .p("WorkThreadCount", workThreadCount())
                    .p("SelectPoolMode", (Epoll.isAvailable() ? "Epoll" : "Nio"))
                    .i();
//            // zombie check
//            startZombieCheck();
//            // monitor
//            startServerMonitor();
            // retry send
            startRetrySend(channelFuture);
            // block
            channelFuture.channel().closeFuture().sync();
        } else {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CenterWsServer)
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
        channelFuture.addListener((ChannelFutureListener) future -> {
            Channel channel = future.channel();
            channel.eventLoop().scheduleAtFixedRate(() -> {
                try {
                    if (ReSendMap.DATA.mappingCount() > 0) {
                        for (Map.Entry<String, ResendMessage<String>> messageObjectEntry : ReSendMap.DATA.entrySet()) {
                            ResendMessage<String> resendMessage = messageObjectEntry.getValue();
                            Channel resendMessageChannel = resendMessage.getChannel();
                            if (!resendMessageChannel.isActive() || !resendMessageChannel.isOpen() || !resendMessageChannel.isWritable()) {
                                ReSendMap.DATA.remove(resendMessage.getReSendId());
                                continue;
                            }
                            AtomicInteger retry = new AtomicInteger(resendMessage.getRetry());
                            if (retry.get() < edgeServerConfig.getRetryNumber()) {
                                String message = resendMessage.getData();
                                resendMessageChannel.eventLoop().execute(() -> {
                                    ChannelFuture retrySendChannelFuture = resendMessageChannel.writeAndFlush(new TextWebSocketFrame(message));
                                    resendMessage.setRetry(retry.incrementAndGet());
                                    retrySendChannelFuture.addListener(retryFuture -> {
                                        if (retryFuture.isDone() && retryFuture.cause() != null) {
                                            KvLogger.instance(this)
//                                                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
//                                                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.EdgeWsServer_ReSendMsgFailed)
//                                                    .p(EdgeConstants.CHANNEL_ATTR_CHANNEL_ID, channel.id())
                                                    .p("ReSendId", resendMessage.getReSendId())
                                                    .p("ReSendMessage", message)
                                                    .p("RetryTimes", retry.get())
                                                    .p(LogFieldConstants.ERR_MSG, retryFuture.cause().getMessage())
                                                    .e(retryFuture.cause());
                                        } else if (retryFuture.isDone() && retryFuture.isSuccess()) {
                                            KvLogger.instance(this)
//                                                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
//                                                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.EdgeWsServer_ReSendMsgSuccessfully)
//                                                    .p(EdgeConstants.CHANNEL_ATTR_CHANNEL_ID, channel.id())
                                                    .p("ReSendId", resendMessage.getReSendId())
                                                    .p("RetryTimes", retry.get())
                                                    .i();
                                            ReSendMap.DATA.remove(resendMessage.getReSendId());
                                        }
                                    });
                                });
                            } else {
                                ReSendMap.DATA.remove(resendMessage.getReSendId());
                            }
                        }
                    }
                } catch (Exception ex) {
                    KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, CenterEvent.CenterWsServer)
                            .p(LogFieldConstants.ACTION, "RetrySend")
                            .e(ex);
                }
            }, 10, 10, TimeUnit.SECONDS);
        });
    }

    @PreDestroy
    public void destroy() {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, CenterEvent.CenterWsServer)
                .p(LogFieldConstants.ACTION, "PrepareDestroy")
                .i();
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
//        if (!zombieCheckScheduler.isShutdown()) {
//            zombieCheckScheduler.shutdown();
//        }
//        channelManager.closeAll();
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, CenterEvent.CenterWsServer)
                .p(LogFieldConstants.ACTION, "DestroySuccessfully")
                .i();
    }
}
