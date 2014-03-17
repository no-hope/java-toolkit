package org.nohope.test;

import org.junit.Test;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2014-03-17 15:33
 */
public class UtilityClassUtilsTest {
    @Test
    public void isUtility() throws Exception {
        UtilityClassUtils.assertUtilityClass(UtilityClassUtils.class);
    }
}
