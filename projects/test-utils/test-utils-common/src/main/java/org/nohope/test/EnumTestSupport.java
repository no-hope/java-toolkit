package org.nohope.test;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.EnumSet;

import static org.junit.Assert.*;

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

        // hm... seems after code generation enum constructor always private ;)
        for (final Constructor<?> c : cons) {
            final int m = c.getModifiers();
            assertTrue("illegal constructor found: " + c,
                    Modifier.isPrivate(m) || (!Modifier.isProtected(m) && !Modifier.isPublic(m)));
        }
    }

    /** @return class which constructor should be tested. */
    protected abstract Class<E> getEnumClass();

    /**
     * Geek check for calling {@code values()} and {@code valueOf(String)}
     * on constructor.
     */
    @Test
    public final void basic() {
        final Iterable<E> set = EnumSet.allOf(getEnumClass());

        for (final E e : set) {
            assertSame(e, Enum.valueOf(getEnumClass(), e.toString()));
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
}
