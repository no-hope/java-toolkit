package org.nohope.test.runner;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-13 04:58
 */
@RunWith(ExpectedExceptionSkippingRunner.class)
public class ExpectedExceptionSkippingRunnerTest {

    @Test
    @SkipOnException(value = IllegalStateException.class, deep = true)
    public void exceptionalDeep() {
        throw new IllegalArgumentException(new IllegalStateException());
    }

    @Test
    @SkipOnException(value = IllegalArgumentException.class)
    public void exceptional() {
        throw new IllegalArgumentException(new IllegalStateException());
    }
}
