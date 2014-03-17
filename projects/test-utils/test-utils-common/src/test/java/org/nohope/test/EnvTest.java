package org.nohope.test;

import org.junit.Test;

/**
 * @since 2013-12-03 16:24
 */
public class EnvTest {
    @Test
    public void isUtility() throws Exception {
        UtilityClassUtils.assertUtilityClass(Env.class);
    }
}
