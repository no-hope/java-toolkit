package org.nohope.test;

import org.junit.Test;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2012-01-28 18:31
 */
public abstract class EnumTestSupport<E extends Enum<E>> {

    /** Tests whatever all enum constructors are private. */
    @Test
    public final void testEnumConstructor() {
        EnumUtils.assertEnumConstructor(getEnumClass());
    }

    /** @return class which constructor should be tested. */
    protected abstract Class<E> getEnumClass();

    /**
     * Geek check for calling {@code values()} and {@code valueOf(String)}
     * on constructor.
     */
    @Test
    public final void basic() {
        EnumUtils.basicAssertions(getEnumClass());
    }

    /**
     * Routine for testing logic which depends on enum order logic.
     *
     * @param values expected order of enums
     */
    @SafeVarargs
    protected final void assertOrder(final E... values) {
        EnumUtils.assertOrder(getEnumClass(), values);
    }
}
