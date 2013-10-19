package org.nohope.test.runner;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assume.assumeNotNull;

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
    @SkipOnException(value = IllegalArgumentException.class, deep = false)
    public void exceptional() {
        throw new IllegalArgumentException(new IllegalStateException());
    }

    @Test(expected = IllegalArgumentException.class)
    public void without() {
        throw new IllegalArgumentException();
    }

    @Test(expected = ClassNotFoundException.class)
    @SkipOnException(value = IllegalArgumentException.class, deep = false)
    public void unexpectedExceptional() throws ClassNotFoundException {
        throw new ClassNotFoundException();
    }

    @Test(expected = ClassNotFoundException.class)
    @SkipOnException(value = IllegalArgumentException.class, deep = true)
    public void unexpectedExceptionalDeep() throws ClassNotFoundException {
        throw new ClassNotFoundException();
    }

    @Test
    public void skipOnAssumption() {
        assumeNotNull(new Object[] {null});
    }
}
