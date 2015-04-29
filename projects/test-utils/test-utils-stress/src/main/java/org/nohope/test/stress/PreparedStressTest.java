package org.nohope.test.stress;

import org.nohope.test.stress.result.StressScenarioResult;
import org.nohope.test.stress.util.Memory;
import org.nohope.test.stress.util.MetricsAccumulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class PreparedStressTest {
    private final int threadsNumber;
    private final int cycleCount;
    private final Iterable<ExecutorService> executors;
    private final Collection<ActionStatsAccumulator> actionStatsAccumulators;
    private final List<Thread> threads;

    PreparedStressTest(final int threadsNumber,
                       final int cycleCount,
                       final Iterable<ExecutorService> executors,
                       final Collection<ActionStatsAccumulator> actionStatsAccumulators,
                       final List<Thread> threads) {
        this.threadsNumber = threadsNumber;
        this.cycleCount = cycleCount;
        this.executors = executors;
        this.actionStatsAccumulators = actionStatsAccumulators;
        this.threads = threads;
    }

    public int getThreadsNumber() {
        return threadsNumber;
    }

    public int getCycleCount() {
        return cycleCount;
    }

    public Iterable<ExecutorService> getExecutors() {
        return executors;
    }

    public Iterable<ActionStatsAccumulator> getActionStatsAccumulators() {
        return actionStatsAccumulators;
    }

    public List<Thread> getThreads() {
        return threads;
    }

    public StressScenarioResult perform() throws InterruptedException {
        final MetricsAccumulator metrics = new MetricsAccumulator();
        metrics.start();

        final Memory memoryStart = Memory.getCurrent();
        final long overallStart = System.nanoTime();

        threads.parallelStream().forEach(Thread::start);

        for (final Thread thread : threads) {
            thread.join();
        }

        metrics.stop();

        for (final ExecutorService service : executors) {
            try {
                service.shutdown();
                service.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
            } catch (final InterruptedException ignored) {
            }
        }

        final long overallEnd = System.nanoTime();
        final Memory memoryEnd = Memory.getCurrent();

        final Collection<StressScenarioResult.ActionStats> actionResults = actionStatsAccumulators
                .parallelStream()
                .map((accumulator) ->
                             new StressScenarioResult.ActionStats(accumulator.getTimesPerThread(),
                                                                  accumulator.getErrorStats(), accumulator.getName()))
                        .collect(toList(actionStatsAccumulators.size()));

        return new StressScenarioResult(
                threadsNumber,
                cycleCount,
                overallStart,
                overallEnd,
                actionResults,
                metrics.getMetrics(),
                memoryStart,
                memoryEnd
            );
    }

    private static <T>
    Collector<T, ?, List<T>> toList(int size) {
        return Collector.of((Supplier<List<T>>) () -> new ArrayList(size),
                            List::add,
                            (left, right) -> {
                                left.addAll(right);
                                return left;
                            });
    }
}
