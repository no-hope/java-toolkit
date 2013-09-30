package org.nohope.test;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import java.security.SecureRandom;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/21/12 6:55 PM
 */
public final class RandomUtils  {
    public static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    public static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String DIGIT = "0123456789";
    public static final String SPECIAL = "~`!@#$%^&*()_+-={}[]:\";'<>?,./|\\";

    private static final SecureRandom RANDOM = new SecureRandom();

    private RandomUtils() {
    }

    /**
     * Generates random string with given length and mask.
     * <p />
     * Mask is a string which may contain next chars:
     * <ul>
     *     <li>{@code A} for {@link RandomUtils#UPPER upper case} letter</li>
     *     <li>{@code a} for {@link RandomUtils#LOWER lower case} letter</li>
     *     <li>{@code #} for {@link RandomUtils#DIGIT digit}</li>
     *     <li>{@code !} for {@link RandomUtils#SPECIAL special character}</li>
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
    public static String nextString(final int length, final String mask) {
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
            result.append(resultMask.charAt(RANDOM.nextInt(resultMask.length())));
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
     * @see RandomUtils#nextString(int, String)
     */
    public static String nextString(final int upperBound) {
        return nextString(RANDOM.nextInt(upperBound), "aA#!");
    }

    /**
     * Generates random string with varying length from {@code 0} to {@code 100} exclusive.
     *
     * @return randomly generated string with varying length
     * @see RandomUtils#nextString(int)
     */
    public static String nextString() {
        return nextString(100);
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
