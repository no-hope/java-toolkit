package org.nohope.test.stress;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:18
*/
public class AbstractStat implements IStressStat {
    private final ConcurrentHashMap<Integer,
            List<Map.Entry<Long,Long>>> timesPerThread = new
            ConcurrentHashMap<>();

    private final AtomicInteger fails = new AtomicInteger(0);
    private final ConcurrentHashMap<Class, AtomicInteger> errorStats = new ConcurrentHashMap<>();
    private final AtomicReference<Result> result = new AtomicReference<>();
    private final String name;
    private final TimerResolution resolution;

    protected AbstractStat(final TimerResolution resolution,
                           final String name) {
        this.resolution = resolution;
        this.name = name;
    }

    @Override
    public final List<Map.Entry<Long, Long>> getInvocationTimes() {
        return null;
    }

    @Override
    public final int getFails() {
        return fails.get();
    }

    @Override
    public final Map<Class, AtomicInteger> getErrorStats() {
        return errorStats;
    }

    protected final <T> T invoke(final int threadId,
                                 final InvocationHandler<T> invoke)
            throws InvocationException {
        timesPerThread.putIfAbsent(threadId,
                new CopyOnWriteArrayList<Map.Entry<Long, Long>>());

        try {
            final long start = resolution.currentTime();
            final T result = invoke.invoke();
            final long diff = resolution.currentTime() - start;
            timesPerThread.get(threadId).add(new ImmutablePair<>(start, diff));
            return result;
        } catch (final Exception e) {
            final Class aClass = e.getClass();
            errorStats.putIfAbsent(aClass, new AtomicInteger(0));
            errorStats.get(aClass).getAndIncrement();
            fails.getAndIncrement();
            throw new InvocationException();
        }
    }

    protected final void calculate() {
        long maxTime = 0;
        long minTime = Long.MAX_VALUE;
        final Map<Long, Long> requests = new HashMap<>();

        long totalDelta = 0L;

        final List<Map.Entry<Long, Long>> times = new ArrayList<>();
        for (final List<Map.Entry<Long, Long>> e : timesPerThread.values()) {
            times.addAll(e);
        }

        final int threadsCount = timesPerThread.size();

        for (final Map.Entry<Long, Long> time : times) {
            final Long runtimeInMillis = time.getValue();
            totalDelta += runtimeInMillis;
            if (maxTime < runtimeInMillis) {
                maxTime = runtimeInMillis;
            }
            if (minTime > runtimeInMillis) {
                minTime = runtimeInMillis;
            }

            final long second = (long) resolution.toSeconds(time.getKey());
            if (!requests.containsKey(second)) {
                requests.put(second, 0L);
            }
            requests.put(second, requests.get(second) + 1);
        }

        final double meanRequestTimeMillis =
                1. * totalDelta / times.size();

        final double totalDeltaSeconds = resolution.toSeconds(totalDelta);
        final double throughput =
                 threadsCount * times.size() / totalDeltaSeconds;

        final double workerThrp =
                times.size() / totalDeltaSeconds;

        result.set(new Result(
                requests,
                meanRequestTimeMillis,
                throughput,
                workerThrp,
                minTime,
                maxTime));
    }

    @Override
    public final String toString() {
        final StringBuilder builder = new StringBuilder();

        final Result res = result.get();
        if (res == null) {
            return "<No result collected>";
        }

        builder.append("----- Stats for (name: ")
               .append(name)
               .append(") -----\n");

        builder.append("Min request time: ")
               .append(resolution.toMillis(res.getMinTime()))
               .append(" ms")
               .append('\n');

        builder.append("Max request time: ")
               .append(resolution.toMillis(res.getMaxTime()))
               .append(" ms")
               .append('\n');

        builder.append("Mean request time: ")
               .append(resolution.toMillis(res.getMeanRequestTime()))
               .append(" ms")
               .append('\n');

        builder.append("Overall errors: ")
               .append(fails.get())
               .append('\n');

        //for (final Map.Entry<Long, Long> e : res.getRequests().entrySet()) {
        //    builder.append(e.getKey())
        //           .append(" : ")
        //           .append(e.getValue())
        //           .append('\n');
        //}

        for (final Map.Entry<Class, AtomicInteger> e : errorStats.entrySet()) {
            builder.append(e.getKey().getName())
                   .append(" happened ")
                   .append(e.getValue().get())
                   .append(" times")
                   .append('\n');
        }

        final List<Map.Entry<Long, Long>> times = new ArrayList<>();
        for (final List<Map.Entry<Long, Long>> e : timesPerThread.values()) {
            times.addAll(e);
        }

        builder.append("Total ops: ")
               .append(times.size())
               .append('\n');

        builder.append("Mean throughput: ")
               .append(res.getThroughput())
               .append(" op/sec")
               .append('\n');

        builder.append("Mean per-worker throughput: ")
                .append(res.getWorkerThrp())
                .append(" op/sec")
                .append('\n');


        return builder.toString();
    }
}
