package org.nohope.test.stress;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final ConcurrentHashMap<Integer, List<Entry<Long, Long>>> timesPerThread = new ConcurrentHashMap<>();
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

    protected final <T> T invoke(final int threadId,
                                 final InvocationHandler<T> invoke)
            throws InvocationException {
        timesPerThread.putIfAbsent(threadId, new CopyOnWriteArrayList<Entry<Long, Long>>());
        try {
            final long start = resolution.currentTime();
            final T result = invoke.invoke();
            final long diff = resolution.currentTime() - start;
            timesPerThread.get(threadId).add(new ImmutablePair<>(start, diff));
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
        long maxTime = 0;
        long minTime = Long.MAX_VALUE;
        long totalDelta = 0L;

        final int threadsCount = timesPerThread.size();

        final Map<Integer, List<Double>> perThreadTimes = new HashMap<>();
        for (final Entry<Integer, List<Entry<Long, Long>>> entry : timesPerThread.entrySet()) {
            final Integer threadId = entry.getKey();
            if (!perThreadTimes.containsKey(threadId)) {
                perThreadTimes.put(threadId, new ArrayList<Double>());
            }

            for (final Entry<Long, Long> period : entry.getValue()) {
                final double diff = resolution.toMillis(period.getValue());
                perThreadTimes.get(threadId).add(diff);
            }
        }

        int size = 0;
        for (final List<Entry<Long, Long>> perThread : timesPerThread.values()) {
            for (final Entry<Long, Long> e : perThread) {
                size++;
                final long runtimeInMillis = e.getValue();
                totalDelta += runtimeInMillis;
                if (maxTime < runtimeInMillis) {
                    maxTime = runtimeInMillis;
                }
                if (minTime > runtimeInMillis) {
                    minTime = runtimeInMillis;
                }
            }
        }

        final double totalDeltaSeconds = resolution.toSeconds(totalDelta);
        final double totalDeltaMillis = resolution.toMillis(totalDelta);
        final double meanRequestTimeMillis = totalDeltaMillis / size;
        final double workerThroughput = size / totalDeltaSeconds;
        final double throughput = threadsCount * workerThroughput;
        result.set(new Result(
                name,
                perThreadTimes,
                errorStats,
                rootErrorStats,
                totalDeltaSeconds,
                meanRequestTimeMillis,
                throughput,
                workerThroughput,
                resolution.toMillis(minTime),
                resolution.toMillis(maxTime)));
    }
}
