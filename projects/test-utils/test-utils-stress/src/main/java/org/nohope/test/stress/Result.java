package org.nohope.test.stress;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math.stat.descriptive.rank.Percentile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Map.Entry;
import static java.util.concurrent.TimeUnit.*;
import static org.nohope.test.stress.TimeUtils.*;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:19
*/
public class Result {
    private final Map<Long, List<Entry<Long, Long>>> timestampsPerThread = new HashMap<>();
    private final Map<Class<?>, List<Exception>> errorStats = new HashMap<>();
    private final Map<Class<?>, List<Throwable>> rootErrorStats = new HashMap<>();

    private final String name;

    private final double meanRequestTime;
    private final long minTime;
    private final long maxTime;
    private final double totalDeltaNanos;
    private final long operationsCount;
    private final int numberOfThreads;
    private final Map<Long, Pair<Long, Long>> startEndForThread;
    private double avgWastedNanos;
    private double avgRuntimeIncludingWastedNanos;
    private final Set<Double> percentiles = new TreeSet<>();
    private final Percentile percentile = new Percentile();

    public Result(final String name,
                  final Map<Long, List<Entry<Long, Long>>> timestampsPerThread,
                  final Map<Class<?>, List<Exception>> errorStats,
                  final long totalDeltaNanos,
                  final long minTime,
                  final long maxTime) {
        this.name = name;
        this.totalDeltaNanos = totalDeltaNanos;
        this.minTime = minTime;
        this.maxTime = maxTime;

        long result = 0;
        for (final List<Entry<Long, Long>> entries : timestampsPerThread.values()) {
            result += entries.size();
        }
        this.operationsCount = result;
        this.meanRequestTime = 1.0 * totalDeltaNanos / operationsCount;

        this.errorStats.putAll(errorStats);
        this.rootErrorStats.putAll(computeRootStats(this.errorStats));


        this.timestampsPerThread.putAll(timestampsPerThread);

        this.numberOfThreads = timestampsPerThread.size();

        startEndForThread = new HashMap<>();
        for (final Entry<Long, List<Entry<Long, Long>>> entries : timestampsPerThread.entrySet()) {
            if (!entries.getValue().isEmpty()) {
                long minStart = Long.MAX_VALUE;
                long maxEnd = 0;
                for (final Entry<Long, Long> e : entries.getValue()) {
                    final Long start = e.getKey();
                    final Long end = e.getValue();
                    if (start < minStart) {
                        minStart = start;
                    }
                    if (end > maxEnd) {
                        maxEnd = end;
                    }
                }

                startEndForThread.put(entries.getKey(), new ImmutablePair<>(minStart, maxEnd));
            }
        }

        avgWastedNanos = 0;
        avgRuntimeIncludingWastedNanos = 0;
        for (final Entry<Long, Pair<Long, Long>> entry : startEndForThread.entrySet()) {
            final Long threadId = entry.getKey();
            final Long minStart = entry.getValue().getKey();
            final Long maxEnd = entry.getValue().getValue();
            double threadDelta = 0;
            for (final Entry<Long, Long> delta : timestampsPerThread.get(threadId)) {
                threadDelta += (delta.getValue() - delta.getKey());
            }

            final double delta = maxEnd - minStart;
            avgRuntimeIncludingWastedNanos += delta;
            avgWastedNanos += (delta - threadDelta);
        }
        avgWastedNanos /= numberOfThreads;
        avgRuntimeIncludingWastedNanos /= numberOfThreads;
        percentiles.add(99.5d);
        percentiles.add(95d);
        percentiles.add(50d);
    }


    private static Map<? extends Class<?>, ? extends List<Throwable>> computeRootStats(final Map<Class<?>, List<Exception>> errorStats) {
        final Map<Class<?>, List<Throwable>> rStats = new HashMap<>(errorStats.size());

        for (final List<Exception> exceptions : errorStats.values()) {
            for (final Exception e: exceptions) {
                Throwable root = ExceptionUtils.getRootCause(e);
                if (root == null) {
                    root = e;
                }
                final Class<?> rClass = root.getClass();
                rStats.computeIfAbsent(rClass, clazz -> new ArrayList<>()).add(root);

            }

        }


        return rStats;
    }


    public double getAvgWastedNanos() {
        return avgWastedNanos;
    }

    public double getAvgRuntimeIncludingWastedNanos() {
        return avgRuntimeIncludingWastedNanos;
    }

    /**
     * @return test scenario name
     */
    public String getName() {
        return name;
    }

    /**
     * @return average request time in nanos
     */
    public double getMeanTime() {
        return meanRequestTime;
    }

    /**
     * @return minimum request time in nanos
     */
    public double getMinTime() {
        return minTime;
    }

    /**
     * @return maximum request time in nanos
     */
    public double getMaxTime() {
        return maxTime;
    }

    /**
     * @return overall op/nanos
     */
    public double getThroughput() {
        return getWorkerThroughput() * numberOfThreads;
    }

    /**
     * @return op/nanos per thread
     */
    public double getWorkerThroughput() {
        return operationsCount / totalDeltaNanos;
    }

    /**
     * @return get pure running time in nanos
     */
    public double getRuntime() {
        return totalDeltaNanos;
    }

    /**
     * @return in nanoseconds
     */
    public Map<Long, List<Long>> getPerThreadRuntimes() {
        final Map<Long, List<Long>> times = new HashMap<>();
        for (final Entry<Long, List<Entry<Long, Long>>> perThreadTime : timestampsPerThread.entrySet()) {
            final List<Long> perThread = perThreadTime.getValue().stream().map(e -> e.getValue() - e.getKey())
                                                      .collect(Collectors.toList());
            times.put(perThreadTime.getKey(), perThread);
        }
        return times;
    }

    /**
     * @return list of all exceptions thrown during test scenario
     */
    public List<Exception> getErrors() {
        final List<Exception> result = new ArrayList<>();
        errorStats.values().forEach(result::addAll);
        return result;
    }

    /**
     * @return list of all exceptions split by topmost exception class
     */
    public Map<Class<?>, List<Exception>> getErrorsPerClass() {
        return Collections.unmodifiableMap(errorStats);
    }

    /**
     * @return list of all running times of each thread in nanos
     */
    public final List<Long> getRunTimes() {
        final List<Long> times = new ArrayList<>();
        for (final List<Entry<Long, Long>> perThreadTime : timestampsPerThread.values()) {
            times.addAll(perThreadTime.stream().map(e -> e.getValue() - e.getKey()).collect(Collectors.toList()));
        }
        return times;
    }

    public Result withPercentiles(final double... percentiles) {
        for (final double percentile : percentiles) {
            if (percentile < 0 || percentile > 100) {
                throw new IllegalStateException("Percentile " + percentile + " is out of range [0, 100]");
            }
        }

        this.percentiles.clear();
        this.percentiles.addAll(Arrays.asList(ArrayUtils.toObject(percentiles)));
        return this;
    }

    @Override
    public final String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("----- Stats for (name: ")
               .append(name)
               .append(") -----\n");

        builder.append(pad("Operations:"))
               .append(operationsCount)
               .append('\n');

        builder.append(pad("Operation time"))
                .append('\n');

        builder.append(pad("  Min:"))
               .append(String.format("%.3f", timeTo(minTime, MILLISECONDS)))
               .append(" ms")
               .append('\n');

        builder.append(pad("  Max:"))
               .append(String.format("%.3f", timeTo(maxTime, MILLISECONDS)))
               .append(" ms")
               .append('\n');

        builder.append(pad("  Avg:"))
               .append(String.format("%.3f", timeTo(meanRequestTime, MILLISECONDS)))
               .append(" ms")
               .append('\n');

        int fails = 0;
        for (final List<Exception> exceptions : errorStats.values()) {
            fails += exceptions.size();
        }

        if (!percentiles.isEmpty()) {
            final List<Long> runTimes = getRunTimes();
            Collections.sort(runTimes);

            final double[] data = new double[runTimes.size()];

            int i = 0;
            for (final Long runtime : runTimes) {
                data[i] = runtime;
                i++;
            }

            for (final Double p : percentiles) {
                builder.append(pad(String.format("  %sth percentile:", p)))
                        .append(String.format("%.3f", timeTo(percentile.evaluate(data, p), MILLISECONDS)))
                        .append(" ms")
                        .append('\n');
            }
        }

        builder.append(pad("Objective avg runtime:"))
                .append(String.format("%.3f", timeTo(totalDeltaNanos / numberOfThreads, SECONDS)))
                .append(" sec\n");

        builder.append(pad(String.format("Total running time (%d workers):", numberOfThreads)))
               .append(String.format("%.3f", timeTo(totalDeltaNanos, SECONDS)))
               .append(" sec\n");

        builder.append(pad("Total time per thread:"))
               .append(String.format("%.3f", timeTo(avgRuntimeIncludingWastedNanos, SECONDS)))
               .append(" sec\n");

        builder.append(pad("Running time per thread:"))
               .append(String.format("%.3f", timeTo(totalDeltaNanos / numberOfThreads, SECONDS)))
               .append(" sec\n");

        builder.append(pad("Avg wasted time per thread:"))
               .append(String.format("%.3e", timeTo(avgWastedNanos, MILLISECONDS)))
               .append(" ms\n");

        builder.append(pad("Avg thread throughput:"))
               .append(String.format("%.3e", throughputTo(getWorkerThroughput(), SECONDS)))
               .append(" op/sec")
               .append('\n');

        builder.append(pad("Avg throughput:"))
               .append(String.format("%.3e", throughputTo(getThroughput(), SECONDS)))
               .append(" op/sec")
               .append('\n');

        builder.append(pad("Errors count:"))
               .append(fails)
               .append('\n');

        for (final Entry<Class<?>, List<Exception>> e : errorStats.entrySet()) {
            builder.append("| ")
                   .append(e.getKey().getName())
                   .append(" happened ")
                   .append(e.getValue().size())
                   .append(" times")
                   .append('\n');
        }

        if (!errorStats.isEmpty()) {
            builder.append("Roots:\n");
            for (final Entry<Class<?>, List<Throwable>> e : rootErrorStats.entrySet()) {
                builder.append("| ")
                       .append(e.getKey().getName())
                       .append(" happened ")
                       .append(e.getValue().size())
                       .append(" times")
                       .append('\n');
            }
        }

        return builder.toString();
    }

    private static String pad(final String str) {
        final int padSize = 40;
        return StringUtils.rightPad(str, padSize, '.');
    }
}
