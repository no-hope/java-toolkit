package org.nohope.test.stress;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-27 23:51
 */
public enum TimerResolution {
    NANOSECONDS("ns", 3) {
        @Override
        public long currentTime() {
            return System.nanoTime();
        }
    },
    MILLISECONDS("ms", 1) {
        @Override
        public long currentTime() {
            return System.currentTimeMillis();
        }
    };

    private final double factor;
    private final String name;

    TimerResolution(final String name, final double factor) {
        this.factor = factor;
        this.name = name;
    }

    public abstract long currentTime();

    public String getName() {
        return name;
    }

    public double toSeconds(final double time) {
        return time / Math.pow(1000, factor);
    }

    public double toMillis(final double time) {
        return time / Math.pow(1000, factor - 1);
    }
}
