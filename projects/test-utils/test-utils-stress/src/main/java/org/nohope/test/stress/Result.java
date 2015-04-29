package org.nohope.test.stress;

import org.nohope.test.stress.result.ActionResult;
import org.nohope.test.stress.result.StressMetrics;
import org.nohope.test.stress.result.StressResult;
import org.nohope.test.stress.util.Measurement;
import org.nohope.test.stress.util.Memory;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2015-04-29 15:05
 */
public class Result {
    private final Collection<StatAccumulator> accumulators = new ArrayList<>();
    private final Collection<StressMetrics> metrics = new ArrayList<>();

    private final long startNanos;
    private final long endNanos;
    private final int threadsNumber;
    private final int cycleCount;

    private final Memory memoryStart;
    private final Memory memoryEnd;

    public Result(final int threadsNumber,
                  final int cycleCount,
                  final long startNanos,
                  final long endNanos,
                  final Collection<StatAccumulator> accumulators,
                  final Collection<StressMetrics> metrics,
                  final Memory memoryStart,
                  final Memory memoryEnd) {
        this.startNanos = startNanos;
        this.endNanos = endNanos;
        this.threadsNumber = threadsNumber;
        this.cycleCount = cycleCount;
        this.accumulators.addAll(accumulators);
        this.metrics.addAll(metrics);
        this.memoryStart = memoryStart;
        this.memoryEnd = memoryEnd;
    }

    public void visitError(final ErrorProcessor processor) {
        for (final StatAccumulator accumulator : accumulators) {
            final String name = accumulator.getName();
            for (Entry<Long, Collection<InvocationException>> e : accumulator.getErrorStats().entrySet()) {
                for (InvocationException m : e.getValue()) {
                    processor.process(name, e.getKey(), m.getCause(), m.getStartNanos(), m.getEndNanos());
                }
            }
        }
    }

    public void visitResult(final ResultProcessor processor) {
        for (final StatAccumulator accumulator : accumulators) {
            final String name = accumulator.getName();
            for (Entry<Long, Collection<Measurement>> e : accumulator.getTimesPerThread().entrySet()) {
                for (Measurement m : e.getValue()) {
                    processor.process(name, e.getKey(), m.getStartNanos(), m.getEndNanos());
                }
            }
        }
    }

    @Deprecated
    public StressResult asResult() {
        final double runtime = endNanos - startNanos;
        final Map<String, ActionResult> results = new HashMap<>();
        for (final StatAccumulator accumulator : accumulators) {
            long maxTimeNanos = 0;
            long minTimeNanos = Long.MAX_VALUE;
            long totalDeltaNanos = 0L;

            for (final Collection<Measurement> perThread : accumulator.getTimesPerThread().values()) {
                for (final Measurement e : perThread) {
                    final long runtimeNanos = e.getEndNanos() - e.getStartNanos();
                    totalDeltaNanos += runtimeNanos;
                    if (maxTimeNanos < runtimeNanos) {
                        maxTimeNanos = runtimeNanos;
                    }
                    if (minTimeNanos > runtimeNanos) {
                        minTimeNanos = runtimeNanos;
                    }
                }
            }

            final Map<Long, Collection<InvocationException>> errorStats = accumulator.getErrorStats();
            final Map<Class<?>, Collection<Exception>> eStats = new HashMap<>();

            for (final Entry<Long, Collection<InvocationException>> entry: errorStats.entrySet()) {
                for (InvocationException ex : entry.getValue()) {
                    final Exception e = (Exception) ex.getCause();
                    eStats.computeIfAbsent(e.getClass(), x -> new ArrayList<>()).add(e);
                }
            }

            results.put(accumulator.getName(), new ActionResult(
                    accumulator.getName(),
                    accumulator.getTimesPerThread(),
                    eStats,
                    totalDeltaNanos,
                    minTimeNanos,
                    maxTimeNanos));
        }

        return new StressResult(results, threadsNumber, cycleCount, runtime, metrics,
                memoryStart, memoryEnd);
    }

    @FunctionalInterface
    public interface ResultProcessor {
        void process(final String name, final long threadId, long startNanos, long endNanos);
    }

    @FunctionalInterface
    public interface ErrorProcessor {
        void process(final String name, final long threadId, final Throwable e, long startNanos, long endNanos);
    }
}
