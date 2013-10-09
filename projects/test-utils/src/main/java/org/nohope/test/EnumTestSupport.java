package org.nohope.test;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.EnumSet;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2012-01-28 18:31
 */
public abstract class EnumTestSupport<E extends Enum<E>> {

    /** Tests whatever all enum constructors are private. */
    @Test
    public final void testEnumConstructor() {
        final Constructor<?>[] cons =
                getEnumClass().getDeclaredConstructors();

        // hm... seems after code generation enum constructor always private
        // ;)
        for (final Constructor<?> c : cons) {
            assertTrue("non-private constructor found",
                    Modifier.isPrivate(c.getModifiers()));
            assertFalse("protected constructor found",
                    Modifier.isProtected(c.getModifiers()));
        }
    }

    /** @return class which constructor should be tested. */
    protected abstract Class<E> getEnumClass();

    /**
     * Geek check for calling {@code values()} and {@code valueOf(String)}
     * on constructor.
     *
     * @throws InvocationTargetException on {@code valueOf(String)} invoke
     * problem
     * @throws NoSuchMethodException not possible
     * @throws IllegalAccessException not possible
     */
    @Test
    public final void basic()
            throws InvocationTargetException, NoSuchMethodException,
                   IllegalAccessException {
        final Iterable<E> set = EnumSet.allOf(getEnumClass());

        for (final E e : set) {
            assertSame(e, valueOf(e, e.toString()));
        }
    }

    /**
     * Routine for testing logic which depends on enum order logic.
     *
     * @param values expected order of enums
     */
    @SafeVarargs
    protected final void assertOrder(final E... values) {
        final Collection<E> set = EnumSet.allOf(getEnumClass());

        assertEquals("All enum values should be passed",
                set.size(),
                values.length);

        int i = 0;
        for (final E v : values) {
            assertEquals("Unexpected order value for " + v, i, v.ordinal());
            i++;
        }
    }

    private Object valueOf(final E e, final String val)
            throws NoSuchMethodException, InvocationTargetException,
                   IllegalAccessException {
        final Method m =
            e.getClass()
             .getDeclaredMethod("valueOf", new Class[]{String.class});
        return m.invoke(e, val);
    }
}
