package org.nohope.test;

import org.junit.Test;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 11/16/11 9:00 PM
 */
public abstract class UtilitiesTestSupport<T> {

    @Test
    public final void testUtilityConstructor() throws Exception {
        UtilityClassUtils.assertUtilityClass(getUtilityClass());
    }

    protected abstract Class<T> getUtilityClass();
}
