package org.nohope.test.stress;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-27 23:51
 */
public enum TimerResolution {
    NANOSECONDS(3) {
        @Override
        public long currentTime() {
            return System.nanoTime();
        }
    },
    MILLISECONDS(1) {
        @Override
        public long currentTime() {
            return System.currentTimeMillis();
        }
    };

    private final double factor;

    TimerResolution(final double factor) {
        this.factor = factor;
    }

    public abstract long currentTime();

    public double toSeconds(final double time) {
        return time / Math.pow(1000, factor);
    }

    public double toMillis(final double time) {
        return time / Math.pow(1000, factor - 1);
    }
}
