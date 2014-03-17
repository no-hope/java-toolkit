package org.nohope.test;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.EnumSet;

import static java.lang.reflect.Modifier.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2012-01-28 18:31
 */
public final class EnumUtils {

    private EnumUtils() {
    }

    /** Tests whatever all enum constructors are private. */
    public static <E extends Enum<E>> void assertEnumConstructor(final Class<E> clazz) {
        final Constructor<?>[] cons = clazz.getDeclaredConstructors();

        // hm... seems after code generation enum constructor always private ;)
        for (final Constructor<?> c : cons) {
            final int m = c.getModifiers();
            assertThat("illegal constructor found: " + c,
                    isPrivate(m) || (!isProtected(m) && !isPublic(m)), equalTo(true));
        }
    }

    /**
     * Geek check for calling {@code values()} and {@code valueOf(String)}
     * on constructor.
     */
    public static <E extends Enum<E>> void basicAssertions(final Class<E> clazz) {
        final Iterable<E> set = EnumSet.allOf(clazz);

        for (final E e : set) {
            assertThat(Enum.valueOf(clazz, e.toString()), sameInstance(e));
        }
    }

    /**
     * Routine for testing logic which depends on enum order logic.
     *
     * @param values expected order of enums
     */
    @SafeVarargs
    protected static <E extends Enum<E>> void assertOrder(final Class<E> clazz, final E... values) {
        final Collection<E> set = EnumSet.allOf(clazz);
        assertThat("All enum values should be passed", set.size(), equalTo(values.length));

        int i = 0;
        for (final E v : values) {
            assertThat("Unexpected order value for " + v, i, equalTo(v.ordinal()));
            i++;
        }
    }


}
