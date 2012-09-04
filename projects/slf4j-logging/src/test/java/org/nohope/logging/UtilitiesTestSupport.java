package org.nohope.logging;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Simple test for properly defined utility constructor.
 * <p/>
 * Example usage:
 * <pre>
 *     public abstract class MyUtilityTest extends UtilitiesTestSupport {
 *         protected abstract Class&lt;?&gt; getUtilityClass() {
 *             return MyUtility.class;
 *         }
 *     }
 * </pre>
 *
 * @author <a href="mailto:vsminkov@rentabiliweb.net">Vsevolod Minkov</a>
 * @since 11/16/11 9:00 PM
 */
public abstract class UtilitiesTestSupport {

    /**
     * Tests whatever class have only one constructor ant it is
     * private.
     *
     * @throws IllegalAccessException on instantiating problem
     * @throws InstantiationException on instantiating problem
     * @throws InvocationTargetException on instantiating problem
     */
    @Test
    public final void testUtilityConstructor()
            throws InvocationTargetException, IllegalAccessException,
                   InstantiationException {
        final Constructor<?>[] cons =
                getUtilityClass().getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPrivate(cons[0].getModifiers()));
        cons[0].setAccessible(true);
        cons[0].newInstance((Object[]) null);
    }

    /** @return class which constructor should be tested. */
    protected abstract Class<?> getUtilityClass();
}
