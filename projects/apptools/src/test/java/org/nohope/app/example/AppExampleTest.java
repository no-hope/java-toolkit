package org.nohope.app.example;

import org.junit.Test;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/15/12 4:14 PM
 */
public class AppExampleTest {

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
    }
}
