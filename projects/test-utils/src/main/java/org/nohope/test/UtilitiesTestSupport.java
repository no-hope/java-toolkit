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
        final Constructor<?>[] cons = getUtilityClass().getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPrivate(cons[0].getModifiers()));
        cons[0].setAccessible(true);
        cons[0].newInstance((Object[]) null);
    }

    protected abstract Class<?> getUtilityClass();
}
