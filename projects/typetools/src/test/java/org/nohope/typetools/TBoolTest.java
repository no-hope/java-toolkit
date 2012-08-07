package org.nohope.typetools;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Date: 07.08.12
 * Time: 17:02
 */
public class TBoolTest {
    @Test
    public void testBoolTool() {
        assertEquals(true, TBool.asBoolean(true));
        assertEquals(false, TBool.asBoolean(false));
        assertEquals(false, TBool.asBoolean(null));

        assertEquals(true, TBool.safeAsBoolean(true));
        assertEquals(false, TBool.safeAsBoolean(false));
        assertEquals(false, TBool.safeAsBoolean(null));
        assertEquals(false, TBool.safeAsBoolean(new Object()));
        assertEquals(false, TBool.safeAsBoolean(1));
    }
}
