package org.nohope.test.stress.result.simplified;

import com.google.common.collect.Sets;
import org.nohope.test.stress.result.StressScenarioResult;
import org.nohope.test.stress.result.StressScenarioResult.ActionStats;
import org.nohope.test.stress.result.StressScenarioResult.ErrorProcessor;
import org.nohope.test.stress.result.StressScenarioResult.Interpreter;
import org.nohope.test.stress.result.StressScenarioResult.ResultProcessor;

import java.util.*;

/**
 */
public class SimpleInterpreter implements Interpreter<SimpleStressResult> {
    @Override
    public SimpleStressResult interpret(final StressScenarioResult result) {
        final double runtime = result.getEndNanos()- result.getStartNanos();

        final Map<String, SimpleActionResult> results = new HashMap<>();
        for (final ActionStats accumulator : result.getActionStats()) {
            final MinMaxTotal minmax = new MinMaxTotal();
            result.visitResult(minmax);

            final Errors errors = new Errors();
            result.visitError(errors);

            final int numberOfThreads = Sets.union(minmax.getThreadIds(), errors.getThreadIds()).size();
            results.put(accumulator.getActionName(), new SimpleActionResult(
                    accumulator.getActionName(),
                    accumulator.getTimesPerThread(),
                    errors.getErrorStats(),
                    numberOfThreads,
                    minmax.getTotalDeltaNanos(),
                    minmax.getMinTimeNanos(),
                    minmax.getMaxTimeNanos()));
        }

        return new SimpleStressResult(results,
                                result.getThreadsNumber(),
                                result.getCycleCount(),
                                runtime,
                                result.getMetrics(),
                                result.getMemoryStart(),
                                result.getMemoryEnd());
    }

    private static class MinMaxTotal implements ResultProcessor {
        private long maxTimeNanos;
        private long totalDeltaNanos;
        private long minTimeNanos = Long.MAX_VALUE;
        private final Set<Long> threadIds = new HashSet<>();

        @Override
        public void process(final String name, final long threadId, final long startNanos, final long endNanos) {
            final long runtimeNanos = endNanos - startNanos;
            this.totalDeltaNanos = totalDeltaNanos + runtimeNanos;
            if (maxTimeNanos < runtimeNanos) {
                this.maxTimeNanos = runtimeNanos;
            }
            if (minTimeNanos > runtimeNanos) {
                this.minTimeNanos = runtimeNanos;
            }
            threadIds.add(threadId);
        }

        public long getMaxTimeNanos() {
            return maxTimeNanos;
        }

        public long getTotalDeltaNanos() {
            return totalDeltaNanos;
        }

        public long getMinTimeNanos() {
            return minTimeNanos;
        }

        public Set<Long> getThreadIds() {
            return Collections.unmodifiableSet(threadIds);
        }
    }

    private static class Errors implements ErrorProcessor {
        private final Map<Class<?>, Collection<Throwable>> eStats = new HashMap<>();
        private final Set<Long> threadIds = new HashSet<>();

        @Override
        public void process(final String name, final long threadId, final Throwable e, final long startNanos, final long endNanos) {
            eStats.computeIfAbsent(e.getClass(), x -> new ArrayList<>()).add(e);
            threadIds.add(threadId);
        }

        public Map<Class<?>, Collection<Throwable>> getErrorStats() {
            return Collections.unmodifiableMap(eStats);
        }

        public Set<Long> getThreadIds() {
            return Collections.unmodifiableSet(threadIds);
        }
    }
}
