//package org.yx.hoststack.edge;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.yx.hoststack.edge.server.ws.session.Session;
//import org.yx.hoststack.edge.server.ws.session.SessionManager;
//import org.yx.hoststack.edge.server.ws.session.SessionType;
//import org.yx.lib.utils.logger.KvLogger;
//
//import java.util.concurrent.ForkJoinPool;
//
//@SpringBootTest
//class EdgeApplicationTests {
//
//	@Test
//	void contextLoads() {
//
//        ForkJoinPool.commonPool().execute(() -> {
//            KvLogger.instance(EdgeApplication.class).p("CreateSession===============", "CreateSession===============").i();
//            SessionManager sessionManager = Context.getBean(SessionManager.class);
//            Session session1 = sessionManager.createSessionTest("Session1", SessionType.Host, 60);
//            new Thread(() -> {
//                while (true) {
//                    try {
//                        Thread.sleep(10000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                    session1.tick();
//                }
//            }).start();
//
//            for (int index = 2; index <= 3; index++) {
//                sessionManager.createSessionTest("Session" + index, SessionType.Host, 10);
//            }
//        });


//        try {
//            String plainText = "8cgwv3t9oiqtgck0zgk8k22oprxpswiwlc2is8deosqlsdy52f71heh1ymcwajeu";
//            String cipherText = YxCryptoUtils.encrypt(plainText);
//            System.out.println(cipherText);
//            String deEncryptStr = YxCryptoUtils.deEncrypt(cipherText);
//            System.out.println(deEncryptStr);
//        } catch (Exception ex) {
//            System.out.println(ex.getMessage());
//        }
//	}
//
//}




//        List<Mono<R<?>>> monoList = Lists.newArrayList();
//        List<R<?>> rList = Lists.newArrayList();
//        for (int i = 0; i < 10; i++) {
//            monoList.add(
//                    storageSvcApiService.createBaseVolume(new CreateBaseVolumeReq()).flatMap(r -> {
//                        System.out.println(JSON.toJSONString(r));
//                        rList.add(r);
//                        return Mono.just(r);
//                    }));
//        }
//        Flux.fromIterable(monoList).flatMap(rMono -> rMono).doOnComplete(() -> {
//            System.out.println(rList.size());
//        }).subscribe();

// test
//        executor.execute(() -> {
//            MessageQueues messageQueues = SpringContextHolder.getBean(MessageQueues.class);
//            for (int i = 0; i < 900000; i++) {
//                messageQueues.getJobNotifyToDiskQueue().add(AgentCommonMessage.builder()
//                        .method("CreateVM")
//                        .hostId(UUID.fastUUID().toString())
//                        .type(MessageType.NOTIFY)
//                        .traceId(UUID.fastUUID().toString())
//                        .jobId(UUID.fastUUID() + "-" + "hostId")
//                        .progress(100)
//                        .status("success")
//                        .code(0)
//                        .build());
//                if (i % 100 == 0) {
//                    try {
//                        TimeUnit.MILLISECONDS.sleep(50);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//        });