package org.nohope.app.example;

import org.nohope.logging.Logger;
import org.nohope.logging.LoggerFactory;
import org.nohope.app.AsyncApp;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/15/12 4:06 PM
 */
public class AsyncAppExample extends AsyncApp {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncAppExample.class);

    @Override
    protected void onPlannedStop() {
        LOG.debug("AsyncAppExample.onPlannedStop()");
    }

    @Override
    protected void onStart() {
        LOG.debug("AsyncAppExample.onStart()");
    }

    @Override
    protected void onVMShutdown() {
        LOG.debug("AsyncAppExample.onVMShutdown()");
    }

}
