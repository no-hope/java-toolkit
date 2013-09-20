package org.nohope.app;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/17/12 11:32 PM
 */
public interface AppShutdownHandler {
    void onPlannedStop();
    void onForcedShutdown();
    void onPlannedShutdown();
}
