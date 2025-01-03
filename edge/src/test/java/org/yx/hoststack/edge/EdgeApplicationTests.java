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
//	}
//
//}
