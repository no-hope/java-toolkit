package org.nohope.typetools;

import org.junit.Test;
import org.nohope.reflection.TypeReference;

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

    @Test
    public void testNull() {
        final Boolean value1 = Cast.as(null, Boolean.class);
        assertNull(value1);
    }


    @Test(expected = java.lang.NullPointerException.class)
    public void testCastExceptional2() {
        final boolean value2 = Cast.as(null, Boolean.class);
        assertNull(value2);
    }

    // type reference

    private static final TypeReference<Boolean> trb = new TypeReference<Boolean>() {};

    @Test
    public void testCastTR() {
        final boolean value1 = Cast.as(true, trb);
        assertEquals(true, value1);

        final Boolean value2 = Cast.as(23.0, trb);
        assertNull(value2);
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void testCastExceptionalTR() {
        final boolean value2 = Cast.as(23.0, trb);
        assertNull(value2);
    }

    @Test
    public void testNullTR() {
        final Boolean value1 = Cast.as(null, trb);
        assertNull(value1);
    }


    @Test(expected = java.lang.NullPointerException.class)
    public void testCastExceptionalTR2() {
        final boolean value2 = Cast.as(null, trb);
        assertNull(value2);
    }
}
