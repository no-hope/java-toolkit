package org.nohope.test;

import org.junit.Test;

import java.io.Serializable;

import static org.junit.Assert.assertEquals;
import static org.nohope.test.SerializationUtils.cloneJava;

/**
 * Just
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/21/12 1:06 AM
 */
public class SerializationUtilsTest {
    @Test
    public void basic() {
        final FinalFieldNoDefaultConstructor origin = new FinalFieldNoDefaultConstructor(1);
        final FinalFieldNoDefaultConstructor result = cloneJava(origin);
        assertEquals(origin.state, result.state);
    }

    private static final class FinalFieldNoDefaultConstructor implements Serializable {
        private static final long serialVersionUID = 6087841492843627140L;
        private final int state;

        private FinalFieldNoDefaultConstructor(final int state) {
            this.state = state;
        }
    }
}
