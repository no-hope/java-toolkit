package org.nohope.test;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2011-11-11/21/11 15:16
 */
public class UtilitiesTestSupportTest extends UtilitiesTestSupport<MyUtility> {
    @Override
    protected Class<MyUtility> getUtilityClass() {
        return MyUtility.class;
    }
}
