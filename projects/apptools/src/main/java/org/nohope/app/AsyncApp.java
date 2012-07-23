package org.nohope.app;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/15/12 3:23 PM
 */
public abstract class AsyncApp extends App {
    private final AtomicBoolean started = new AtomicBoolean(false);
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
        started.set(false);
        latch.countDown();
    }

    @Override
    public final void start() throws InterruptedException {
        super.start();
        started.set(true);
        latch.await();
    }

    @Override
    protected final void onVMShutdownWrapper() {
        super.onVMShutdownWrapper();
        started.set(false);
        latch.countDown();
    }

    public final boolean isStarted() {
        return started.get();
    }
}
