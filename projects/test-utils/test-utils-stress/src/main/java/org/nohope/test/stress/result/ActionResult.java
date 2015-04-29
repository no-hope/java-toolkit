package org.nohope.test.stress.result;

import com.google.common.base.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.nohope.test.stress.util.Measurement;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.util.Map.Entry;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.nohope.test.stress.util.TimeUtils.throughputTo;
import static org.nohope.test.stress.util.TimeUtils.timeTo;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-12-27 16:19
 */
@Deprecated
public class ActionResult {
    private static final Function<Measurement, Long> DIFF = e -> e.getEndNanos() - e.getStartNanos();

    private final Map<Long, Collection<Measurement>> timestampsPerThread = new HashMap<>();
    private final Map<Class<?>, Collection<Exception>> errorStats = new HashMap<>();
    private final Map<Class<?>, Collection<Throwable>> rootErrorStats = new HashMap<>();
    private final Map<Long, Measurement> startEndForThread;

    private final String name;

    private final double meanRequestTime;
    private final long minTime;
    private final long maxTime;
    private final double totalDeltaNanos;
    private final long operationsCount;
    private final int numberOfThreads;
    private double avgWastedNanos;
    private double avgRuntimeIncludingWastedNanos;
    private final Set<Double> percentiles = new TreeSet<>();
    private final Percentile percentile = new Percentile();

    public ActionResult(final String name,
                        final Map<Long, Collection<Measurement>> timestampsPerThread,
                        final Map<Class<?>, Collection<Exception>> errorStats,
                        final long totalDeltaNanos,
                        final long minTime,
                        final long maxTime) {
        this.name = name;
        this.totalDeltaNanos = totalDeltaNanos;
        this.minTime = minTime;
        this.maxTime = maxTime;

        this.operationsCount = timestampsPerThread.values().stream().flatMap(Collection::stream).count();
        this.meanRequestTime = 1.0 * totalDeltaNanos / operationsCount;

        this.errorStats.putAll(errorStats);
        this.rootErrorStats.putAll(computeRootStats(this.errorStats));
        this.timestampsPerThread.putAll(timestampsPerThread);
        this.numberOfThreads = timestampsPerThread.size();

        startEndForThread = timestampsPerThread
                .entrySet().stream().filter(entries -> !entries.getValue().isEmpty())
                .collect(Collectors.toMap(Entry::getKey, e -> Measurement.of(
                        calc(LongStream::min, e.getValue(), Measurement::getStartNanos),
                        calc(LongStream::max, e.getValue(), Measurement::getEndNanos))));

        avgWastedNanos = 0;
        avgRuntimeIncludingWastedNanos = 0;
        startEndForThread.entrySet().forEach(entry -> {
            final double threadDelta = timestampsPerThread
                    .get(entry.getKey()).stream().map(DIFF).reduce(0L, Long::sum);

            final double delta = DIFF.apply(entry.getValue());
            avgRuntimeIncludingWastedNanos += delta;
            avgWastedNanos += (delta - threadDelta);
        });

        avgWastedNanos /= numberOfThreads;
        avgRuntimeIncludingWastedNanos /= numberOfThreads;
        percentiles.add(99.5d);
        percentiles.add(95d);
        percentiles.add(50d);
    }

    private static Map<Class<?>, List<Throwable>> computeRootStats(final Map<Class<?>, Collection<Exception>> errorStats) {
        //noinspection ThrowableResultOfMethodCallIgnored
        return errorStats.values().parallelStream().flatMap(Collection::stream)
                         .map(e -> Objects.firstNonNull(ExceptionUtils.getRootCause(e), e))
                         .collect(Collectors.groupingBy(Object::getClass));
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
     * Per thread timestamps of operation start and end
     *
     * @return in nanoseconds
     */
    public Map<Long, Collection<Measurement>> getTimestampsPerThread() {
        return Collections.unmodifiableMap(timestampsPerThread);
    }

    /**
     * Operation times per thread
     *
     * @return in nanoseconds
     */
    public Map<Long, List<Long>> getPerThreadRuntimes() {
        return timestampsPerThread.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e ->
                e.getValue().stream().map(DIFF).collect(Collectors.toList())));
    }

    /**
     * @return list of all exceptions thrown during test scenario
     */
    public List<Exception> getErrors() {
        return errorStats.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * @return list of all exceptions split by topmost exception class
     */
    public Map<Class<?>, Collection<Exception>> getErrorsPerClass() {
        return Collections.unmodifiableMap(errorStats);
    }

    /**
     * @return list of all running times of each thread in nanos
     */
    public final List<Long> getRunTimes() {
        return timestampsPerThread.values().parallelStream()
                                  .flatMap(t -> t.stream().map(DIFF))
                                  .collect(Collectors.toList());
    }

    public ActionResult withPercentiles(final double... percentiles) {
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

        long fails = errorStats.values().stream().flatMap(Collection::stream).count();
        if (!percentiles.isEmpty()) {
            final List<Long> runTimes = getRunTimes();
            Collections.sort(runTimes);

            final double[] data = new double[runTimes.size()];
            int i = 0;
            for (final Long runtime : runTimes) {
                data[i] = runtime;
                i++;
            }

            percentiles.forEach(p ->
                    builder.append(pad(String.format("  %sth percentile:", p)))
                           .append(String.format("%.3f", timeTo(percentile.evaluate(data, p), MILLISECONDS)))
                           .append(" ms")
                           .append('\n'));
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

        errorStats.entrySet().forEach(e ->
                builder.append("| ")
                       .append(e.getKey().getName())
                       .append(" happened ")
                       .append(e.getValue().size())
                       .append(" times")
                       .append('\n'));

        if (!errorStats.isEmpty()) {
            builder.append("Roots:\n");
            rootErrorStats.entrySet().forEach(e ->
                    builder.append("| ")
                           .append(e.getKey().getName())
                           .append(" happened ")
                           .append(e.getValue().size())
                           .append(" times")
                           .append('\n'));
        }

        return builder.toString();
    }

    private static String pad(final String str) {
        final int padSize = 40;
        return StringUtils.rightPad(str, padSize, '.');
    }

    private static <T> long calc(final Function<LongStream, OptionalLong> param,
                                 final Collection<T> measurements,
                                 final ToLongFunction<T> getter) {
        return param.apply(measurements.stream().mapToLong(getter)).getAsLong();
    }
}
