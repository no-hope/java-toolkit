package org.nohope.test.stress;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.nohope.test.stress.action.Get;
import org.nohope.test.stress.action.Invoke;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Map.Entry;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:18
*/
class StatCalculator {
    private final ConcurrentHashMap<Long, List<Entry<Long, Long>>> timesPerThread = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, ConcurrentLinkedQueue<Exception>> errorStats = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, ConcurrentLinkedQueue<Throwable>> rootErrorStats = new ConcurrentHashMap<>();
    private final AtomicReference<Result> result = new AtomicReference<>();
    private final AtomicInteger fails = new AtomicInteger(0);
    private final String name;
    private final TimerResolution resolution;
    private final int concurrency;


    protected StatCalculator(final TimerResolution resolution,
                             final String name, final int concurrency) {
        this.resolution = resolution;
        this.name = name;
        this.concurrency = concurrency;

        for (long threadId = 0L; threadId < concurrency; ++threadId) {
            // it's safe to use ArrayList here, they are always modified by same thread!
            timesPerThread.put(threadId, new ArrayList<>());
        }
    }

    @Nonnull
    public Result getResult() {
        if (result.get() == null) {
            calculate();
        }
        return result.get();
    }

    protected final <T> T invoke(final long threadId,
                                 final Get<T> invoke)
            throws InvocationException {
        try {
            final long start = resolution.currentTime();
            final T result = invoke.get();
            final long end = resolution.currentTime();
            timesPerThread.get(threadId).add(new ImmutablePair<>(start, end));
            return result;
        } catch (final Exception e) {
            handleException(e);
            throw new InvocationException();
        }
    }

    protected final void invoke(final long threadId,
                                 final Invoke invoke)
            throws InvocationException {
        try {
            final long start = resolution.currentTime();
            invoke.invoke();
            final long end = resolution.currentTime();
            timesPerThread.get(threadId).add(new ImmutablePair<>(start, end));
        } catch (final Exception e) {
            handleException(e);
            throw new InvocationException();
        }
    }


    private void handleException(final Exception e) {
        final Class<?> aClass = e.getClass();
        errorStats.computeIfAbsent(aClass, clazz -> new ConcurrentLinkedQueue<>()).add(e);
        fails.getAndIncrement();
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

        final Map<Class<?>, List<Exception>> eStats = new HashMap<>(errorStats.size());

        for (final Entry<Class<?>, ConcurrentLinkedQueue<Exception>> entry: errorStats
                .entrySet()) {
            eStats.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }


        result.set(new Result(
                name,
                timesPerThread,
                eStats,
                totalDeltaNanos,
                minTimeNanos,
                maxTimeNanos));
    }


    protected int getConcurrency() {
        return concurrency;
    }
}
