package org.nohope.test.stress;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-27 23:51
 */
public enum TimerResolution {
    NANOSECONDS(1000 * 1000 * 1000, "ns") {
        @Override
        public long currentTime() {
            return System.nanoTime();
        }
    },
    MILLISECONDS(1000, "ms") {
        @Override
        public long currentTime() {
            return System.currentTimeMillis();
        }
    };

    private final double factor;
    private final String name;

    TimerResolution(final double factor, final String name) {
        this.factor = factor;
        this.name = name;
    }

    public abstract long currentTime();

    public String getName() {
        return name;
    }

    public double getFactor() {
        return factor;
    }
}
