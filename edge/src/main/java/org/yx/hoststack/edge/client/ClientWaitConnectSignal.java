package org.yx.hoststack.edge.client;

import org.yx.lib.utils.util.CountDownLatch2;

import java.util.concurrent.TimeUnit;

public class ClientWaitConnectSignal {
    private static final CountDownLatch2 Signal = new CountDownLatch2(1);

    public static boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return Signal.await(timeout, unit);
    }

    public static void release() {
        Signal.countDown();
    }


    public static void reset() {
        if (Signal.getCount() != 1) {
            Signal.reset();
        }
    }
}
