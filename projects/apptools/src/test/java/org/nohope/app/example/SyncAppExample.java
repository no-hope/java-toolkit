package org.nohope.app.example;

import org.nohope.logging.Logger;
import org.nohope.logging.LoggerFactory;
import org.nohope.app.App;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/15/12 4:06 PM
 */
public class SyncAppExample extends App {
    private static final Logger LOG = LoggerFactory.getLogger(SyncAppExample.class);
    private boolean done = false;

    public void stop() {
        done = true;
        LOG.debug("SyncAppExample.stop()");
    }

    @Override
    protected void onStart() {
        LOG.debug("SyncAppExample.onStart()");
        while (!done) {
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                LOG.debug(e, "Sleep interrupted");
            }
        }
    }

    @Override
    protected void onVMShutdown() {
        LOG.debug("SyncAppExample.onVMShutdown()");
    }
}
