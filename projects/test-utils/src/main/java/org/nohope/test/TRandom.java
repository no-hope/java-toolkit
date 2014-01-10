package org.nohope.test;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/21/12 6:55 PM
 */
public final class TRandom {
    public static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    public static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String DIGIT = "0123456789";
    public static final String SPECIAL = "~`!@#$%^&*()_+-={}[]:\";'<>?,./|\\";

    private final Callable<Random> randomGetter;

    private TRandom(final Callable<Random> randomGetter) {
        this.randomGetter = randomGetter;
    }

    /** @return TRandom instance based on {@link ThreadLocalRandom} class */
    public static TRandom threadLocal() {
        return new TRandom(new Callable<Random>() {
            @Override
            public Random call() throws Exception {
                return ThreadLocalRandom.current();
            }
        });
    }

    /** @return TRandom instance based on {@link Random} class */
    public static TRandom standard() {
        return new TRandom(new Callable<Random>() {
            private final Random rnd = new Random();

            @Override
            public Random call() throws Exception {
                return rnd;
            }
        });
    }

    /** @return TRandom instance based on single random instance */
    public static TRandom singleton(@Nonnull final Random rnd) {
        return new TRandom(new Callable<Random>() {
            @Override
            public Random call() throws Exception {
                return rnd;
            }
        });
    }

    /** @return TRandom instance based on thread-local random instance */
    public static TRandom threadLocal(@Nonnull final Callable<Random> rnd) {
        final ThreadLocal<Random> local = new ThreadLocal<Random>() {
            @Override
            protected Random initialValue() {
                try {
                    return rnd.call();
                } catch (final Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        };

        return new TRandom(new Callable<Random>() {
            @Override
            public Random call() throws Exception {
                return local.get();
            }
        });
    }

    private Random get() {
        try {
            final Random rnd = randomGetter.call();
            if (rnd != null) {
                return rnd;
            }
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

        throw new IllegalStateException("random instance is null");
    }

    /**
     * Generates random string with given length and mask.
     * <p />
     * Mask is a string which may contain next chars:
     * <ul>
     *     <li>{@code A} for {@link TRandom#UPPER upper case} letter</li>
     *     <li>{@code a} for {@link TRandom#LOWER lower case} letter</li>
     *     <li>{@code #} for {@link TRandom#DIGIT digit}</li>
     *     <li>{@code !} for {@link TRandom#SPECIAL special character}</li>
     * </ul>
     *
     * Example:
     * <pre>
     *     RandomUtils.nextString(10, "#A!"); // `XH(^=>70;
     * </pre>
     *
     * @param length target string length
     * @param mask mask
     * @return randomly generated string
     */
    public String nextString(final int length, final String mask) {
        String resultMask = "";
        if (mask.indexOf('a') > -1) {
            resultMask += LOWER;
        }
        if (mask.indexOf('A') > -1) {
            resultMask += UPPER;
        }
        if (mask.indexOf('#') > -1) {
            resultMask += DIGIT;
        }
        if (mask.indexOf('!') > -1) {
            resultMask += SPECIAL;
        }

        if ("".equals(mask)) {
            throw new IllegalArgumentException("Mask should contain at last one char from 'aA#!' sequence");
        }

        final StringBuilder result = new StringBuilder();
        for (int i = length; i > 0; --i) {
            result.append(resultMask.charAt(get().nextInt(resultMask.length())));
        }
        return result.toString();
    }

    /**
     * Generates random string with varying length from {@code 0} to given upper bound value exclusive.
     * <p />
     * This version of string generator users {@code "aA#!"} mask.
     *
     * @param upperBound length
     * @return randomly generated string with varying length
     * @see TRandom#nextString(int, String)
     */
    public String nextString(final int upperBound) {
        return nextString(get().nextInt(upperBound), "aA#!");
    }

    /**
     * Generates random string with varying length from {@code 0} to {@code 100} exclusive.
     *
     * @return randomly generated string with varying length
     * @see TRandom#nextString(int)
     */
    public String nextString() {
        return nextString(100);
    }

    public int nextInt() {
        return get().nextInt();
    }

    public DateTime nextUtcDateTime() {
        return DateTime.now(ISOChronology.getInstanceUTC());
    }

    public long nextLong() {
        return get().nextLong();
    }

    public boolean nextBoolean() {
        return get().nextBoolean();
    }

    public double nextDouble() {
        return get().nextDouble();
    }
}
