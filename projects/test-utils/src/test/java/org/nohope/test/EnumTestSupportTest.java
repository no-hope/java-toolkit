package org.nohope.test;

import org.junit.Test;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2012-01-29 12:30
 */
public class EnumTestSupportTest extends EnumTestSupport<MyEnum> {
    @Override
    protected Class<MyEnum> getEnumClass() {
        return MyEnum.class;
    }

    @Test
    public void order() {
        assertOrder(MyEnum.ONE, MyEnum.TWO, MyEnum.THREE, MyEnum.FOUR);
    }
}
