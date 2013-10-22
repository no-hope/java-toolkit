package org.nohope;

import org.nohope.typetools.TStr;

import javax.annotation.Nonnull;

/**
 * This utility implements short-circuit operations on matchers.
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-22 15:46
 */
public class Matchers {
    private Matchers() {
    }

    @SafeVarargs
    public static <T> IMatcher<T> and(final IMatcher<T>... matchers) {
        return new IMatcher<T>() {
            @Override
            public boolean matches(@Nonnull final T obj) {
                for (final IMatcher<T> m : matchers) {
                    if (!m.matches(obj)) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public String toString() {
                return "(" + TStr.join(matchers, " && ") + ")";
            }
        };
    }

    @SafeVarargs
    public static <T> IMatcher<T> or(final IMatcher<T>... matchers) {
        return new IMatcher<T>() {
            @Override
            public boolean matches(@Nonnull final T obj) {
                for (final IMatcher<T> m : matchers) {
                    if (m.matches(obj)) {
                        return true;
                    }
                }

                return false;
            }

            @Override
            public String toString() {
                return "(" + TStr.join(matchers, " || ") + ")";
            }
        };
    }

    public static <T> IMatcher<T> not(final IMatcher<T> matcher) {
        return new IMatcher<T>() {
            @Override
            public boolean matches(@Nonnull final T obj) {
                return !matcher.matches(obj);
            }

            @Override
            public String toString() {
                return "!" + matcher;
            }
        };
    }
}
