package org.nohope.test.stress.util;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

import static java.lang.StrictMath.pow;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2014-01-20 15:40
 */
public final class TimeUtils {
    private TimeUtils() {
    }

    public static double timeTo(final double nanoseconds, final TimeUnit unit) {
        return convert(nanoseconds, unit, false);
    }

    public static double throughputTo(final double nanoseconds, final TimeUnit unit) {
        return convert(nanoseconds, unit, true);
    }

    private static double convert(final double nanoseconds,
                                  @Nonnull final TimeUnit unit,
                                  final boolean throughput) {
        final int mul = throughput ? 1 : -1;
        switch (unit) {
            case NANOSECONDS: return nanoseconds;
            case MICROSECONDS: return power(nanoseconds, mul * 3);
            case MILLISECONDS: return power(nanoseconds, mul * 6);
            case SECONDS: return power(nanoseconds, mul * 9);
            case MINUTES: return pow(6, mul) * power(nanoseconds, mul * 10);
            case HOURS: return pow(36, mul) * power(nanoseconds, mul * 11);
            case DAYS: return pow(864, mul) * power(nanoseconds, mul * 11);
            default: throw new IllegalStateException("unsupported unit " + unit);
        }
    }

    private static double power(final double time, final int pow) {
        return time * pow(10L, pow);
    }
}
