package org.nohope.test.runner;


import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-13 04:53
 */
@RunWith(InstanceTestClassRunner.class)
public class InstanceTestClassRunnerTest implements InstanceTestSetupListener {
    private final AtomicBoolean before = new AtomicBoolean();

    @Override
    public void beforeClassSetup() {
        before.set(true);
    }

    @Override
    public void afterClassSetup() {
    }

    @Test
    public void begin() {
        assertTrue(before.get());
    }

    @Test
    public void beginOnCreated() {
        assertTrue(before.get());
    }
}
