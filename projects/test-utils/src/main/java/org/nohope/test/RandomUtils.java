package org.nohope.test;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/21/12 6:55 PM
 * @deprecated use {@link org.nohope.test.TRandom} instead
 */
@Deprecated
public final class RandomUtils  {
    public static final String LOWER = TRandom.LOWER;
    public static final String UPPER = TRandom.UPPER;
    public static final String DIGIT = TRandom.DIGIT;
    public static final String SPECIAL = TRandom.SPECIAL;

    private static final TRandom RANDOM = TRandom.threadLocal();

    private RandomUtils() {
    }

    public static String nextString(final int length, final String mask) {
        return RANDOM.nextString(length, mask);
    }

    public static String nextString(final int upperBound) {
        return RANDOM.nextString(upperBound);
    }
    public static String nextString() {
        return RANDOM.nextString();
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
