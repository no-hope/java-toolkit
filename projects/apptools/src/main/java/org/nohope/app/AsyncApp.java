package org.nohope.app;

import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/15/12 3:23 PM
 */
public abstract class AsyncApp extends App {
    private final CountDownLatch latch;

    protected abstract void onStop();

    protected AsyncApp() {
        this(1);
    }

    protected AsyncApp(final int count) {
        latch = new CountDownLatch(count);
    }

    public final void stop() {
        onStop();
        latch.countDown();
    }

    @Override
    public final void start() throws InterruptedException {
        super.start();
        latch.await();
    }

    @Override
    protected final void onVMShutdownWrapper() {
        super.onVMShutdownWrapper();
        latch.countDown();
    }
}
