package org.nohope.test.stress;

import org.nohope.test.stress.util.Memory;
import org.nohope.test.stress.util.MetricsAccumulator;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class PreparedMeasurement {
    private final int threadsNumber;
    private final int cycleCount;
    private final Iterable<ExecutorService> executors;
    private final Collection<StatAccumulator> statAccumulators;
    private final List<Thread> threads;

    PreparedMeasurement(final int threadsNumber,
                        final int cycleCount,
                        final Iterable<ExecutorService> executors,
                        final Collection<StatAccumulator> statAccumulators,
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

    public Iterable<StatAccumulator> getStatAccumulators() {
        return statAccumulators;
    }

    public List<Thread> getThreads() {
        return threads;
    }

    public Result perform() throws InterruptedException {
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

        return new Result(
                threadsNumber,
                cycleCount,
                overallStart,
                overallEnd,
                statAccumulators,
                metrics.getMetrics(),
                memoryStart,
                memoryEnd
            );
    }
}
