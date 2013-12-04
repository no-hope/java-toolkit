package org.nohope.test;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 11/16/11 9:00 PM
 */
public abstract class UtilitiesTestSupport {

    //XXX: is it really worth it?
    @Test
    public final void testUtilityConstructor() throws Exception {
        final Class<?> clazz = getUtilityClass();
        assertTrue("Utility class " + clazz.getCanonicalName() + " should be declared final",
                Modifier.isFinal(clazz.getModifiers()));

        final Constructor<?>[] cons = clazz.getDeclaredConstructors();
        final String errorMessage = "Utility class "
                                  + clazz.getCanonicalName()
                                  + " should have only one private "
                                  + "default constructor";
        assertEquals(errorMessage, 1, cons.length);

        final Constructor<?> ctor = cons[0];
        assertEquals(errorMessage, 0, ctor.getParameterTypes().length);
        assertTrue(errorMessage, Modifier.isPrivate(ctor.getModifiers()));
        ctor.setAccessible(true);
        ctor.newInstance((Object[]) null);
    }

    protected abstract Class<?> getUtilityClass();
}
