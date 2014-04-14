package org.nohope.reflection;

import org.junit.Test;
import org.nohope.test.EnumTestSupport;

import static com.google.common.base.Predicates.*;
import static org.junit.Assert.*;
import static org.nohope.reflection.ModifierMatcher.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-11 13:39
 */
public class ModifierMatcherTest extends EnumTestSupport<ModifierMatcher> {

    @Override
    protected Class<ModifierMatcher> getEnumClass() {
        return ModifierMatcher.class;
    }

    @Test
    public void equals() {
        assertTrue(equalTo(1).apply(1));
        assertTrue(equalTo(null).apply(null));
        assertFalse(equalTo("test").apply(null));
        assertFalse(equalTo(null).apply("test"));
    }

    @Test
    public void reprTest() {
        assertEquals("Not(Or(And(PUBLIC,ABSTRACT),FINAL))", not(or(and(PUBLIC, ABSTRACT), FINAL)).toString());
    }
}
