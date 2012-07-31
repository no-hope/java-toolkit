package org.nohope.typetools;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * Date: 31.07.12
 * Time: 15:18
 */
public class CastTest {

    @Test
    public void testCast() {
        final boolean value1 = Cast.as(true, Boolean.class);
        assertEquals(true, value1);

        final Boolean value2 = Cast.as(23.0, Boolean.class);
        assertNull(value2);
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void testCastExceptional() {
        final boolean value2 = Cast.as(23.0, Boolean.class);
        assertNull(value2);
    }
}
