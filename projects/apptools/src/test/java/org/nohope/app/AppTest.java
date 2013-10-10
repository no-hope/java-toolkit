package org.nohope.app;

import org.junit.Test;
import org.nohope.app.example.AsyncAppExample;
import org.nohope.app.example.SyncAppExample;

import static org.junit.Assert.assertFalse;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/15/12 4:14 PM
 */
public class AppTest {

    @Test
    public void syncApp() throws Exception {
        final SyncAppExample test = new SyncAppExample();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    /* do nothing */
                }
                test.stop();
            }
        }).start();

        test.start();
    }

    @Test
    public void asyncApp() throws Exception {
        final AsyncAppExample test = new AsyncAppExample();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    /* do nothing */
                }
                test.stop();
            }
        }).start();

        test.start();
        assertFalse(test.isStarted());
    }

    @Test
    public void asyncAppForcedShutdown() throws Exception {
        final AsyncAppExample test = new AsyncAppExample();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    /* do nothing */
                }
                test.onVMShutdownWrapper();
            }
        }).start();

        test.start();
        assertFalse(test.isStarted());
    }
}
