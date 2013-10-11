package org.nohope.typetools;

import org.junit.Test;
import org.nohope.test.UtilitiesTestSupport;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Date: 07.08.12
 * Time: 17:02
 */
public class TDoubleTest extends UtilitiesTestSupport {

    @Override
    protected Class<?> getUtilityClass() {
        return TDouble.class;
    }

    @Test
    public void doubleCorrectness() {
        assertTrue(TDouble.isDoubleCorrect(1.0));
        assertFalse(TDouble.isDoubleCorrect(Double.NaN));
        assertFalse(TDouble.isDoubleCorrect(Double.POSITIVE_INFINITY));
        assertFalse(TDouble.isDoubleCorrect(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void finiteDouble() {
        assertNull(TDouble.toFiniteDouble(Double.NEGATIVE_INFINITY));
        assertNull(TDouble.toFiniteDouble(Double.POSITIVE_INFINITY));
        assertNull(TDouble.toFiniteDouble(Double.NaN));
        assertEquals((Double) 88.0, TDouble.toFiniteDouble(88.0));
    }

    @Test
    public void converting() {
        assertEquals(1.0, TDouble.asDouble(null, 1.0), 10e-6);
        assertEquals(2.0, TDouble.asDouble(2.0, 1.0), 10e-6);
        assertEquals(1.0, TDouble.asDouble(Double.NEGATIVE_INFINITY, 1.0), 10e-6);
        assertEquals(1.0, TDouble.asDouble(Double.POSITIVE_INFINITY, 1.0), 10e-6);
        assertEquals(1.0, TDouble.asDouble(Double.NaN, 1.0), 10e-6);
    }
}
