package org.nohope.test.stress;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-27 23:51
 */
public enum TimerResolution {
    NANOSECONDS {
        @Override
        public long currentTime() {
            return System.nanoTime();
        }
    },
    MILLISECONDS {
        @Override
        public long currentTime() {
            return System.currentTimeMillis() * 1000000;
        }
    };

    /** @return current time in <b>nanoseconds</b> */
    public abstract long currentTime();
}
