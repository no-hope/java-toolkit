package org.nohope;

import org.junit.Test;
import org.nohope.test.UtilitiesTestSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.nohope.Matchers.*;
import static org.nohope.reflection.ModifierMatcher.ABSTRACT;
import static org.nohope.reflection.ModifierMatcher.FINAL;
import static org.nohope.reflection.ModifierMatcher.PUBLIC;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-22 16:31
 */
public class MatchersTest extends UtilitiesTestSupport {
    @Override
    protected Class<?> getUtilityClass() {
        return Matchers.class;
    }

    @Test
    public void equals() {
        assertTrue(eq(1).matches(1));
        assertTrue(eq(null).matches(null));
        assertFalse(eq("test").matches(null));
        assertFalse(eq(null).matches("test"));
    }

    @Test
    public void reprTest() {
        assertEquals("!((PUBLIC && ABSTRACT) || FINAL)", not(or(and(PUBLIC, ABSTRACT), FINAL)).toString());
    }
}
