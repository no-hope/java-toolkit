package org.nohope.test;

import org.junit.Test;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2014-03-17 16:06
 */
public class EnumUtilsTest {
    @Test
    public void isUtil() throws Exception {
        UtilityClassUtils.assertUtilityClass(EnumUtils.class);
    }

    @Test
    public void enumTests() {
        EnumUtils.basicAssertions(MyEnum.class);
        EnumUtils.assertEnumConstructor(MyEnum.class);
        EnumUtils.assertOrder(MyEnum.class, MyEnum.ONE, MyEnum.TWO, MyEnum.THREE, MyEnum.FOUR);
    }
}
