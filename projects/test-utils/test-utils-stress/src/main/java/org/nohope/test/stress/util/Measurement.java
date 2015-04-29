package org.nohope.test.stress.util;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2015-04-29 04:12
 */
public final class Measurement {
    private final long startNanos;
    private final long endNanos;

    public Measurement(final long startNanos, final long endNanos) {
        this.startNanos = startNanos;
        this.endNanos = endNanos;
    }

    public long getStartNanos() {
        return startNanos;
    }

    public long getEndNanos() {
        return endNanos;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Measurement that = (Measurement) o;
        return startNanos == that.startNanos
            && endNanos == that.endNanos;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(startNanos);
        result = 31 * result + Long.hashCode(endNanos);
        return result;
    }

    public static Measurement of(final long start, final long end) {
        return new Measurement(start, end);
    }
}
