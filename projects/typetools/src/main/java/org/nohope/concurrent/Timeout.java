package org.nohope.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * Simple timeout implementation.
 * <p/>
 * This class wraps {@link TimeUnit} class and stores it's value.
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 8/02/11 5:24 PM
 */
public final class Timeout {
    /** Time in timeout time unit. */
    private final long aTime;
    /** Time unit. */
    private final TimeUnit timeUnit;

    /**
     * Creates timeout with given time in given time unit.
     *
     * @param time time in given time unit
     * @param unit time unit
     */
    public Timeout(final long time, final TimeUnit unit) {
        this.aTime = time;
        this.timeUnit = unit;
    }

    /**
     * Creates timeout with {@link TimeUnit#SECONDS seconds} as a time unit.
     *
     * @param time time in seconds
     */
    public Timeout(final long time) {
        this(time, TimeUnit.SECONDS);
    }

    /** @return time in selected time unit */
    public long time() {
        return aTime;
    }

    /** @return time unit of timeout */
    public TimeUnit timeUnit() {
        return timeUnit;
    }

    /** @return timeout in milliseconds */
    public long milliseconds() {
        return TimeUnit.MILLISECONDS.convert(time(), timeUnit());
    }

    /**
     * Performs {@link Thread#sleep(long)}.
     *
     * @throws InterruptedException if interrupted while sleeping
     */
    public void sleep() throws InterruptedException {
        timeUnit().sleep(time());
    }
}
