package org.nohope.test;

import org.junit.Test;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/30/13 9:02 PM
 */
public class TRandomTest {
    private static final Pattern ALL = Pattern.compile(".");

    private static final TRandom[] RNDS = {
            TRandom.standard(),
            TRandom.threadLocal(),
            TRandom.singleton(new SecureRandom()),
            TRandom.threadLocal(new Callable<Random>() {
                @Override
                public Random call() throws Exception {
                    return new SecureRandom();
                }
            })
    };

    @Test
    public void naive() {
        for (final TRandom rnd : RNDS) {
            rnd.nextInt();
            rnd.nextLong();
            rnd.nextDouble();
            rnd.nextBoolean();
            rnd.nextUtcDateTime();
        }
    }

    @Test
    public void randomString() {
        for (final TRandom rnd : RNDS) {
            assertContainsAll(rnd.nextString(10, "a"), TRandom.LOWER, 10);
            assertContainsAll(rnd.nextString(10, "A"), TRandom.UPPER, 10);
            assertContainsAll(rnd.nextString(10, "#"), TRandom.DIGIT, 10);
            assertContainsAll(rnd.nextString(10, "!"), TRandom.SPECIAL, 10);

            assertTrue(rnd.nextString(10).length() <= 10);
            assertTrue(rnd.nextString().length() <= 100);

            try {
                RandomUtils.nextString(10, "");
                fail();
            } catch (final IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void failure() {
        final TRandom rnd = TRandom.threadLocal(new Callable<Random>() {
            @Override
            public Random call() throws Exception {
                return null;
            }
        });

        try {
            rnd.nextInt();
            fail();
        } catch (final IllegalStateException e) {
        }

        final TRandom rnd2 = TRandom.threadLocal(new Callable<Random>() {
            @Override
            public Random call() throws Exception {
                throw new Exception("test");
            }
        });

        try {
            rnd2.nextInt();
            fail();
        } catch (final IllegalStateException e) {
        }
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
