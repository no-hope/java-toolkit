package org.nohope.test;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/30/13 9:02 PM
 */
public class RandomUtilsTest {
    private static final Pattern ALL = Pattern.compile(".");

    @Test
    public void randomString() {
        assertContainsAll(RandomUtils.nextString(10, "a"), RandomUtils.LOWER, 10);
        assertContainsAll(RandomUtils.nextString(10, "A"), RandomUtils.UPPER, 10);
        assertContainsAll(RandomUtils.nextString(10, "#"), RandomUtils.DIGIT, 10);
        assertContainsAll(RandomUtils.nextString(10, "!"), RandomUtils.SPECIAL, 10);
    }

    private static void assertContainsAll(final String target, final String source, final int length) {
        assertEquals(length, target.length());
        for (final String s : ALL.split(source)) {
            if (!target.contains(s)) {
                fail();
            }
        }
    }
}
