package org.nohope.test.stress.result.simplified;

import com.google.common.collect.Sets;
import org.nohope.test.stress.result.StressScenarioResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public class SimpleInterpreter implements StressScenarioResult.Interpreter<SimpleStressResult> {
    @Override
    public SimpleStressResult interpret(final StressScenarioResult result) {
        final double runtime = result.getEndNanos()- result.getStartNanos();

        final Map<String, SimpleActionResult> results = new HashMap<>();
        for (final StressScenarioResult.ActionStats accumulator : result.getActionStats()) {

            final MinMaxTotal minmax = new MinMaxTotal();
            result.visitResult(minmax);

            final Errors errors = new Errors();
            result.visitError(errors);

            final int numberOfThreads = Sets.union(minmax.threadIds, errors.threadIds).size();
            results.put(accumulator.getActionName(), new SimpleActionResult(
                    accumulator.getActionName(),
                    accumulator.getTimesPerThread(),
                    errors.eStats,
                    numberOfThreads,
                    minmax.totalDeltaNanos,
                    minmax.minTimeNanos,
                    minmax.maxTimeNanos));
        }

        return new SimpleStressResult(results,
                                result.getThreadsNumber(),
                                result.getCycleCount(),
                                runtime,
                                result.getMetrics(),
                                result.getMemoryStart(),
                                result.getMemoryEnd());
    }


    private static class MinMaxTotal implements StressScenarioResult.ResultProcessor {
        long maxTimeNanos = 0;
        long minTimeNanos = Long.MAX_VALUE;
        long totalDeltaNanos = 0L;
        Set<Long> threadIds = new HashSet<>();

        @Override
        public void process(final String name, final long threadId, final long startNanos, final long endNanos) {
            final long runtimeNanos = endNanos - startNanos;
            totalDeltaNanos += runtimeNanos;
            if (maxTimeNanos < runtimeNanos) {
                maxTimeNanos = runtimeNanos;
            }
            if (minTimeNanos > runtimeNanos) {
                minTimeNanos = runtimeNanos;
            }
            threadIds.add(threadId);
        }
    }


    private static class Errors implements StressScenarioResult.ErrorProcessor {
        final Map<Class<?>, Collection<Throwable>> eStats = new HashMap<>();
        Set<Long> threadIds = new HashSet<>();

        @Override
        public void process(final String name, final long threadId, final Throwable e, final long startNanos, final long endNanos) {
            eStats.computeIfAbsent(e.getClass(), x -> new ArrayList<>()).add(e);
            threadIds.add(threadId);
        }
    }
}
