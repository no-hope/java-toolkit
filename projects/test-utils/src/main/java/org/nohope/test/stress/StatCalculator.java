package org.nohope.test.stress;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Map.Entry;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:18
*/
class StatCalculator {
    private final ConcurrentHashMap<Long, List<Entry<Long, Long>>> timesPerThread = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, List<Exception>> errorStats = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, List<Throwable>> rootErrorStats = new ConcurrentHashMap<>();
    private final AtomicReference<Result> result = new AtomicReference<>();
    private final AtomicInteger fails = new AtomicInteger(0);
    private final String name;
    private final TimerResolution resolution;

    protected StatCalculator(final TimerResolution resolution,
                             final String name) {
        this.resolution = resolution;
        this.name = name;
    }

    @Nonnull
    public Result getResult() {
        if (result.get() == null) {
            calculate();
        }
        return result.get();
    }

    protected final <T> T invoke(final long threadId,
                                 final InvocationHandler<T> invoke)
            throws InvocationException {
        // it's safe to use ArrayList here, they are always modified by same thread!
        timesPerThread.putIfAbsent(threadId, new ArrayList<Entry<Long, Long>>());
        try {
            final long start = resolution.currentTime();
            final T result = invoke.invoke();
            final long end = resolution.currentTime();
            timesPerThread.get(threadId).add(new ImmutablePair<>(start, end));
            return result;
        } catch (final Exception e) {
            Throwable root = ExceptionUtils.getRootCause(e);
            if (root == null) {
                root = e;
            }
            final Class aClass = e.getClass();
            errorStats.putIfAbsent(aClass, new CopyOnWriteArrayList<Exception>());
            errorStats.get(aClass).add(e);

            final Class rClass = root.getClass();
            rootErrorStats.putIfAbsent(rClass, new CopyOnWriteArrayList<Throwable>());
            rootErrorStats.get(rClass).add(root);

            fails.getAndIncrement();
            throw new InvocationException();
        }
    }

    private void calculate() {
        long maxTimeNanos = 0;
        long minTimeNanos = Long.MAX_VALUE;
        long totalDeltaNanos = 0L;

        for (final List<Entry<Long, Long>> perThread : timesPerThread.values()) {
            for (final Entry<Long, Long> e : perThread) {
                final long runtimeNanos = e.getValue() - e.getKey(); // end - start
                totalDeltaNanos += runtimeNanos;
                if (maxTimeNanos < runtimeNanos) {
                    maxTimeNanos = runtimeNanos;
                }
                if (minTimeNanos > runtimeNanos) {
                    minTimeNanos = runtimeNanos;
                }
            }
        }

        result.set(new Result(
                name,
                timesPerThread,
                errorStats,
                rootErrorStats,
                totalDeltaNanos,
                minTimeNanos,
                maxTimeNanos));
    }
}
