package org.nohope.reflection;

import org.junit.Test;
import org.nohope.test.EnumTestSupport;

import static org.junit.Assert.assertEquals;
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
    public void reprTest() {
        assertEquals("!((PUBLIC && ABSTRACT) || FINAL)", not(or(and(PUBLIC, ABSTRACT), FINAL)).toString());
    }
}
