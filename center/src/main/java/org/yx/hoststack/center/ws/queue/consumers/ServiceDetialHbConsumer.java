//package org.yx.hoststack.center.ws.queue.consumers;
//
//import com.google.common.collect.Lists;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.yx.hoststack.center.common.CenterEvent;
//import org.yx.hoststack.center.ws.queue.Queues;
//import org.yx.lib.utils.logger.KvLogger;
//import org.yx.lib.utils.logger.LogFieldConstants;
//
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//@Component
//@RequiredArgsConstructor
//public class ServiceDetialHbConsumer implements Runnable {
//
//    private final Queues queues;
//    private final ServiceSession serviceSession;
//
//    @Override
//    public void run() {
//        while (true) {
//            try {
//                List<Long> ids = Lists.newArrayList();
//                queues.getServiceRefreshHealthQueue().drainTo(ids, 50);
//                if (!ids.isEmpty()) {
//                    serviceSession.refreshHealth(ids);
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
