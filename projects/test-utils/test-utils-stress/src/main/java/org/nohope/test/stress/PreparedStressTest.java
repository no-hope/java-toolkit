package org.nohope.test.stress;

import org.nohope.test.stress.result.StressScenarioResult;
import org.nohope.test.stress.result.StressScenarioResult.ActionStats;
import org.nohope.test.stress.util.Memory;
import org.nohope.test.stress.util.MetricsAccumulator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;

public final class PreparedStressTest {
    private final int threadsNumber;
    private final int cycleCount;
    private final Iterable<ExecutorService> executors;
    private final Collection<ActionStatsAccumulator> actionStatsAccumulators;
    private final List<Thread> threads;
    private final Duration metricsInterval;

    PreparedStressTest(final int threadsNumber,
                       final int cycleCount,
                       final Iterable<ExecutorService> executors,
                       final Collection<ActionStatsAccumulator> actionStatsAccumulators,
                       final List<Thread> threads,
                       final Duration metricsInterval) {
        this.threadsNumber = threadsNumber;
        this.cycleCount = cycleCount;
        this.executors = executors;
        this.actionStatsAccumulators = actionStatsAccumulators;
        this.threads = threads;
        this.metricsInterval = metricsInterval;
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
        final MetricsAccumulator metrics = new MetricsAccumulator(metricsInterval);
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

        final Collection<ActionStats> actionResults = actionStatsAccumulators.parallelStream()
               .map(acc -> new ActionStats(acc.getTimesPerThread(), acc.getErrorStats(), acc.getName()))
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

    private static <T> Collector<T, ?, List<T>> toList(int size) {
        return Collector.of(() -> new ArrayList(size), List::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                });
    }
}
