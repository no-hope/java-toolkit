package org.nohope.app;

import org.nohope.logging.Logger;
import org.nohope.logging.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/15/12 3:23 PM
 */
public abstract class AsyncApp extends App {
    private final AtomicReference<AppState> state = new AtomicReference<>(AppState.INITIALIZING);
    private static final Logger LOG = LoggerFactory.getLogger(AsyncApp.class);

    private final CountDownLatch latch;

    protected abstract void onPlannedStop();

    protected void onForcedShutdown() {

    }

    protected void onPlannedShutdown() {

    }

    protected AsyncApp() {
        this(1);
    }

    protected AsyncApp(final int count) {
        latch = new CountDownLatch(count);
    }

    public final void stop() {
        setState(AppState.TERMINATING);
        onPlannedStop();
        setState(AppState.TERMINATED);
        latch.countDown();
    }

    @Override
    public final void start() throws Exception {
        LOG.debug("Performing application startup");
        super.start();
        LOG.debug("Startup routine completed");

        if (!(getState().equals(AppState.TERMINATED) || getState().equals(AppState.TERMINATING))) {
            setState(AppState.RUNNING);
            latch.await();
        } else {
            LOG.warn("Application was terminated due start() routine. Is it really async?");
        }

    }

    @Override
    protected final void onVMShutdownWrapper() {
        LOG.debug("Executing base VM shutdown handler");
        super.onVMShutdownWrapper();
        LOG.debug("VM shutdown handled");

        if (getState().equals(AppState.RUNNING) || getState().equals(AppState.INITIALIZING)) {
            LOG.debug("Termination was forced");
            onForcedShutdown();
        } else {
            LOG.debug("Termination was planned");
            onPlannedShutdown();
        }

        setState(AppState.TERMINATED);
        latch.countDown();
    }

    public final boolean isStarted() {
        return state.get().equals(AppState.RUNNING);
    }

    private AppState getState() {
        return state.get();
    }

    private void setState(final AppState stateValue) {
        LOG.debug("Setting application state to {}", stateValue);
        state.set(stateValue);
    }
}
