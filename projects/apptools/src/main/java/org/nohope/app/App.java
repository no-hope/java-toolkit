package org.nohope.app;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/15/12 3:13 PM
 */
public abstract class App {
    protected abstract void onStart() throws Exception;

    protected void onVMShutdown() {
    }

    public void start() throws Exception {
        Runtime.getRuntime()
                .addShutdownHook(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        onVMShutdownWrapper();
                    }
                }));

        onStart();
    }

    protected void onVMShutdownWrapper() {
        onVMShutdown();
    }
}
