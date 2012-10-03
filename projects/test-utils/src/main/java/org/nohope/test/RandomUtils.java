package org.nohope.test;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import java.security.SecureRandom;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/21/12 6:55 PM
 */
public final class RandomUtils  {
    private static final SecureRandom RANDOM = new SecureRandom();

    private RandomUtils() {
    }

    public static String nextString() {
        return Long.toString(Math.abs(RANDOM.nextLong()));
    }

    public static int nextInt() {
        return RANDOM.nextInt();
    }

    public static DateTime nextUtcDateTime() {
        return DateTime.now(ISOChronology.getInstanceUTC());
    }

    public static long nextLong() {
        return RANDOM.nextLong();
    }

    public static boolean nextBoolean() {

        return RANDOM.nextBoolean();
    }

    public static double nextDouble() {
        return RANDOM.nextDouble();
    }
}
