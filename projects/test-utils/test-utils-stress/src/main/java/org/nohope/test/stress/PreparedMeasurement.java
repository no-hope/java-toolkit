package org.nohope.test.stress;

import org.nohope.test.stress.result.ActionResult;
import org.nohope.test.stress.result.StressResult;
import org.nohope.test.stress.util.Memory;
import org.nohope.test.stress.util.MetricsAccumulator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class PreparedMeasurement {
    private final int threadsNumber;
    private final int cycleCount;
    private final Iterable<ExecutorService> executors;
    private final Iterable<? extends StatAccumulator> statAccumulators;
    private final List<Thread> threads;

    PreparedMeasurement(final int threadsNumber,
                        final int cycleCount,
                        final Iterable<ExecutorService> executors,
                        final Iterable<? extends StatAccumulator> statAccumulators,
                        final List<Thread> threads) {
        this.threadsNumber = threadsNumber;
        this.cycleCount = cycleCount;
        this.executors = executors;
        this.statAccumulators = statAccumulators;
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

    public Iterable<? extends StatAccumulator> getStatAccumulators() {
        return statAccumulators;
    }

    public List<Thread> getThreads() {
        return threads;
    }

    public StressResult perform() throws InterruptedException {
        return awaitResult(this);
    }

    private static StressResult awaitResult(final PreparedMeasurement preparedMeasurement)
            throws InterruptedException {
        final MetricsAccumulator metrics = new MetricsAccumulator();
        metrics.start();

        final Memory memoryStart = Memory.getCurrent();
        final long overallStart = System.nanoTime();

        preparedMeasurement.threads.parallelStream().forEach(Thread::start);

        for (final Thread thread : preparedMeasurement.threads) {
            thread.join();
        }

        metrics.stop();

        for (final ExecutorService service : preparedMeasurement.executors) {
            try {
                service.shutdown();
                service.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
            } catch (final InterruptedException ignored) {
            }
        }

        final long overallEnd = System.nanoTime();
        final Memory memoryEnd = Memory.getCurrent();

        final double runtime = overallEnd - overallStart;

        final Map<String, ActionResult> results = new HashMap<>();
        for (final StatAccumulator stats : preparedMeasurement.statAccumulators) {
            final ActionResult r = stats.getResult();
            results.put(r.getName(), r);
        }

        return new StressResult(results,
                preparedMeasurement.threadsNumber,
                preparedMeasurement.cycleCount,
                runtime,
                metrics.getMetrics(),
                memoryStart,
                memoryEnd);
    }
}
