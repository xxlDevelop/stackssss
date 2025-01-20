package org.yx.hoststack.center.ws;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.enums.RegisterNodeEnum;
import org.yx.hoststack.center.common.redis.util.RedissonUtils;
import org.yx.hoststack.center.entity.*;
import org.yx.hoststack.center.service.*;
import org.yx.hoststack.center.ws.common.ConsistentHashing;
import org.yx.hoststack.center.ws.common.Node;
import org.yx.hoststack.center.ws.config.CenterServerConfig;
import org.yx.hoststack.protocol.ws.ResendMessage;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class CenterServer implements Runnable {
    private static final int availableProcessors = Runtime.getRuntime().availableProcessors() * 3;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final CenterServerConfig edgeServerConfig;

    private final CenterServerChannelInitializer centerServerChannelInitializer;


    public static final String REGION_CACHE_KEY = "regionCache";
    public static final ConcurrentHashMap<String, List<RegionInfo>> globalRegionInfoCacheMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, IdcInfo> globalIdcInfoCacheMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, RelayInfo> globalRelayInfoCacheMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, ServiceDetail> globalServerDetailCacheMap = new ConcurrentHashMap<>();

    private final RegionInfoService regionInfoService;
    private final IdcInfoService idcInfoService;
    private final RelayInfoService relayInfoService;
    private final ServiceDetailService serviceDetailService;
    private final HostService hostService;
    private final ContainerService containerService;
    private final SysModuleService sysModuleService;
    private final AgentCpuService agentCpuService;
    private final AgentGpuService agentGpuService;

    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    public static final ConsistentHashing serverConsistentHash = new ConsistentHashing(1);
    public static final ConsistentHashing hostConsistentHash = new ConsistentHashing(1);
    public static final ConsistentHashing sysModuleConsistentHash = new ConsistentHashing(1);
    public static final ConsistentHashing gpuInfoConsistentHash = new ConsistentHashing(1);
    public static final ConsistentHashing cpuInfoConsistentHash = new ConsistentHashing(1);
    public static final ConsistentHashing containerConsistentHash = new ConsistentHashing(1);

    public static String address;
    public static String hostName;
    public static int port;
    public static Node centerNode;

    @Qualifier("virtualThreadExecutor")
    private final Executor virtualThreadExecutor;

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
                    .p(LogFieldConstants.ERR_MSG, ex.getMessage())
                    .e(ex);
            System.exit(0);
        }
    }

    private void init() throws InterruptedException {


        // init project params
        applicationInit();
        initShard();
        initAgent();

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


    void applicationInit() {
        List<RegionInfo> regionInfos = regionInfoService.list();

        globalRegionInfoCacheMap.put(REGION_CACHE_KEY, regionInfos);

        globalIdcInfoCacheMap = idcInfoService.list(new LambdaQueryWrapper<IdcInfo>().select(IdcInfo::getId, IdcInfo::getZone, IdcInfo::getRegion, IdcInfo::getIdc, IdcInfo::getIdcIp)).parallelStream().collect(Collectors.toConcurrentMap(IdcInfo::getIdc, x -> x, (key1, key2) -> key2, ConcurrentHashMap::new));

        globalRelayInfoCacheMap = relayInfoService.list(new LambdaQueryWrapper<RelayInfo>().select(RelayInfo::getId, RelayInfo::getZone, RelayInfo::getRegion, RelayInfo::getRelay, RelayInfo::getRelayIp)).parallelStream().collect(Collectors.toMap(RelayInfo::getRelay, x -> x, (key1, key2) -> key2, ConcurrentHashMap::new));

        globalServerDetailCacheMap = serviceDetailService.list().parallelStream().collect(Collectors.toMap(ServiceDetail::getServiceId, x -> x, (key1, key2) -> key2, ConcurrentHashMap::new));

        hostName = nacosDiscoveryProperties.getUsername();

        address = nacosDiscoveryProperties.getIp();

        port = nacosDiscoveryProperties.getPort();

        centerNode = new Node(hostName == null ? "center" : hostName, RegisterNodeEnum.CENTER, null);

    }

    void initAgent() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        initAgent(futures);
        initContainer(futures);
        initSysModule(futures);
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    public void initAgent(List<CompletableFuture<Void>> futures) {
        long count = hostService.count();
        int pageSize = 1000;
        long totalPages = (count + pageSize - 1) / pageSize;
        for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
            int finalPageNo = pageNo;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Page<Host> page = new Page<>(finalPageNo, pageSize);

                hostService.page(page, new LambdaQueryWrapper<Host>()
                        .select(Host::getHostId, Host::getResourcePool, Host::getRelay, Host::getIdc,
                                Host::getHostIp, Host::getAk, Host::getSk, Host::getLastHbAt,
                                Host::getZone, Host::getRegion, Host::getDevSn));

                List<Host> sysModules = page.getRecords();

                sysModules.forEach(host -> {
                    initGpuInfo(futures, host.getHostId());
                    initCpuInfo(futures, host.getHostId());
                    String shardKey = hostConsistentHash.getShard(host.getDevSn()).toString();
                    if (shardKey != null && !shardKey.isEmpty()) {
                        RedissonUtils.setLocalCachedMap(shardKey, host.getDevSn(), host);
                    }
                });

            }, virtualThreadExecutor);
            futures.add(future);
        }
    }

    public void initContainer(List<CompletableFuture<Void>> futures) {
        long count = containerService.count();
        int pageSize = 1000;
        long totalPages = (count + pageSize - 1) / pageSize;
        for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
            int finalPageNo = pageNo;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Page<Container> page = new Page<>(finalPageNo, pageSize);
                containerService.page(page);

                List<Container> containers = page.getRecords();

                containers.forEach(c -> {
                    initGpuInfo(futures, c.getContainerId());
                    initCpuInfo(futures, c.getContainerId());
                    String shardKey = containerConsistentHash.getShard(c.getDevSn()).toString();
                    if (shardKey != null && !shardKey.isEmpty()) {
                        RedissonUtils.setLocalCachedMap(shardKey, c.getContainerId(), c);
                    }
                });

            }, virtualThreadExecutor);

            futures.add(future);
        }
    }

    public void initSysModule(List<CompletableFuture<Void>> futures) {
        long count = sysModuleService.count();
        int pageSize = 1000;
        long totalPages = (count + pageSize - 1) / pageSize;
        for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
            int finalPageNo = pageNo;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Page<SysModule> page = new Page<>(finalPageNo, pageSize);
                sysModuleService.page(page, new LambdaQueryWrapper<SysModule>()
                        .select(SysModule::getModuleId, SysModule::getModuleName, SysModule::getModuleArch, SysModule::getModuleConfig,
                                SysModule::getVersion, SysModule::getMd5));

                List<SysModule> sysModules = page.getRecords();

                sysModules.forEach(sys -> {
                    String hash = String.valueOf(Objects.hash(sys.getModuleArch(), sys.getVersion()));
                    String shardKey = sysModuleConsistentHash.getShard(hash).toString();
                    if (shardKey != null && !shardKey.isEmpty()) {
                        RedissonUtils.setLocalCachedMap(shardKey, hash, sys);
                    }
                });

            }, virtualThreadExecutor);

            futures.add(future);
        }
    }

    public void initGpuInfo(List<CompletableFuture<Void>> futures, String agentId) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            List<AgentGpu> gpus = agentGpuService.list(new LambdaQueryWrapper<AgentGpu>().eq(AgentGpu::getAgentId, agentId));

            if(!CollectionUtils.isEmpty(gpus)){
                String shardKey = gpuInfoConsistentHash.getShard(agentId).toString();
                if (shardKey != null && !shardKey.isEmpty()) {
                    RedissonUtils.setLocalCachedMap(shardKey, agentId, gpus);
                }
            }

        }, virtualThreadExecutor);

        futures.add(future);
    }

    public void initCpuInfo(List<CompletableFuture<Void>> futures, String agentId) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
           AgentCpu cpu = agentCpuService.getOne(new LambdaQueryWrapper<AgentCpu>().eq(AgentCpu::getAgentId, agentId),false);

            if(!ObjectUtils.isEmpty(cpu)){
                String shardKey = cpuInfoConsistentHash.getShard(agentId).toString();
                if (shardKey != null && !shardKey.isEmpty()) {
                    RedissonUtils.setLocalCachedMap(shardKey, agentId, cpu);
                }
            }
        }, virtualThreadExecutor);

        futures.add(future);
    }

    public void initShard(){
        for (int i = 0; i < 10; i++) { serverConsistentHash.addShard(String.format("host_stack_center:server_shard_%s", i));};
        for (int i = 0; i < 50; i++) { hostConsistentHash.addShard(String.format("host_stack_center:host_shard_%s", i));};
        for (int i = 0; i < 5; i++) { sysModuleConsistentHash.addShard(String.format("host_stack_center:sysModule_shard_%s", i));};
        for (int i = 0; i < 50; i++) { gpuInfoConsistentHash.addShard(String.format("host_stack_center:gpuInfo_shard_%s", i));};
        for (int i = 0; i < 50; i++) { cpuInfoConsistentHash.addShard(String.format("host_stack_center:cpuInfo_shard_%s", i));};
        for (int i = 0; i < 50; i++) { containerConsistentHash.addShard(String.format("host_stack_center:container_shard_%s", i));};
    }
}
