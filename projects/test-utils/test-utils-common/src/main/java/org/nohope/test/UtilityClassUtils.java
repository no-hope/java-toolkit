package org.nohope.test;

import java.lang.reflect.Constructor;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPrivate;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 11/16/11 9:00 PM
 */
public final class UtilityClassUtils {

    private UtilityClassUtils() {
    }

    public static <T> void assertUtilityClass(final Class<T> clazz) throws Exception {
        final String message = "Utility class "
                               + clazz.getCanonicalName()
                               + " should be declared final";
        assertThat(message, isFinal(clazz.getModifiers()), equalTo(true));

        final Constructor<?>[] cons = clazz.getDeclaredConstructors();
        final String errorMessage = "Utility class "
                                  + clazz.getCanonicalName()
                                  + " should have only one private "
                                  + "default constructor";
        assertThat(errorMessage, cons.length, equalTo(1));

        final Constructor<?> ctor = cons[0];
        assertThat(errorMessage, ctor.getParameterTypes().length, equalTo(0));
        assertThat(errorMessage, isPrivate(ctor.getModifiers()), equalTo(true));
        ctor.setAccessible(true);
        ctor.newInstance((Object[]) null);
    }
}
