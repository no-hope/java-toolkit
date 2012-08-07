package org.nohope.typetools;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * Date: 07.08.12
 * Time: 17:02
 */
public class TDoubleTest {
    @Test
    public void testBoolTool() {
        assertEquals(true, TDouble.isDoubleCorrect(1.0));
        assertEquals(false, TDouble.isDoubleCorrect(Double.NaN));
        assertEquals(false, TDouble.isDoubleCorrect(Double.POSITIVE_INFINITY));
        assertEquals(false, TDouble.isDoubleCorrect(Double.NEGATIVE_INFINITY));

        assertNull(TDouble.toFiniteDouble(Double.NEGATIVE_INFINITY));
        assertNull(TDouble.toFiniteDouble(Double.NaN));
        assertNull(TDouble.toFiniteDouble(Double.POSITIVE_INFINITY));

        assertEquals(88.0, TDouble.toFiniteDouble(88.0));
    }
}
