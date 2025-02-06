//package org.yx.hoststack.center.ws.queue.consumers;
//
//import com.google.common.collect.Lists;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.yx.hoststack.center.common.CenterEvent;
//import org.yx.hoststack.center.ws.queue.Queues;
//import org.yx.hoststack.center.ws.session.Session;
//import org.yx.hoststack.center.ws.session.SessionManager;
//import org.yx.hoststack.center.ws.session.service.ServiceSession;
//import org.yx.lib.utils.logger.KvLogger;
//import org.yx.lib.utils.logger.LogFieldConstants;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//@Component
//@RequiredArgsConstructor
//public class ServiceRegisterConsumer implements Runnable {
//
//    private final Queues queues;
//    private final SessionManager sessionManager;
//
//    @Override
//    public void run() {
//        while (true) {
//            try {
//                List<Session> registerSessions = new ArrayList<>();
//                queues.getServiceRegisterQueue().drainTo(registerSessions, 50);
//                if (!registerSessions.isEmpty()) {
//                    sessionManager.register(registerSessions);
//                } else {
//                    TimeUnit.MILLISECONDS.sleep(10);
//                }
//            } catch (InterruptedException interruptedException) {
//                KvLogger.instance(this)
//                        .p(LogFieldConstants.EVENT, CenterEvent.Center_WS_SERVER)
//                        .p(LogFieldConstants.ACTION, CenterEvent.Action.REFRESH_SERVICE_DETAIL_DB)
//                        .p(LogFieldConstants.ERR_MSG, interruptedException.getMessage())
//                        .e(interruptedException);
//            } catch (Exception e) {
//                KvLogger.instance(this)
//                        .p(LogFieldConstants.EVENT, CenterEvent.Center_WS_SERVER)
//                        .p(LogFieldConstants.ACTION, CenterEvent.Action.REFRESH_SERVICE_DETAIL_DB)
//                        .p(LogFieldConstants.ERR_MSG, e.getMessage())
//                        .e(e);
//            }
//        }
//    }
//}
