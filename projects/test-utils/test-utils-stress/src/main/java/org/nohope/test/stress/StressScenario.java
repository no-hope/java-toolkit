package org.nohope.test.stress;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-12-26 21:39
 */
public class StressScenario {
    private StressScenario(final TimerResolution resolution) {
        this.resolution = resolution;
    }

    private final TimerResolution resolution;

    protected TimerResolution getResolution() {
        return resolution;
    }

    public static StressScenario of(final TimerResolution resolution) {
        return new StressScenario(resolution);
    }

    public StressResult measure(final int threadsNumber,
                               final int cycleCount,
                               final NamedAction... actions)
            throws InterruptedException {

        final Map<String, SingleInvocationStatCalculator> stats = new HashMap<>();
        for (final NamedAction action : actions) {
            stats.put(action.getName(),
                    new SingleInvocationStatCalculator(resolution, action, threadsNumber));
        }

        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadsNumber; i++) {
            final int k = i;
            threads.add(new Thread(() -> {
                for (int j = k * cycleCount; j < (k + 1) * cycleCount; j++) {
                    try {
                        for (final SingleInvocationStatCalculator stat : stats.values()) {
                            stat.invoke(k, j);
                        }
                    } catch (final InvocationException e) {
                        // TODO
                    }
                }
            }, "stress-worker-" + k));
        }

        final Memory memoryStart = Memory.getCurrent();
        final long overallStart = resolution.currentTime();
        threads.forEach(java.lang.Thread::start);

        for (final Thread thread : threads) {
            thread.join();
        }
        final long overallEnd = resolution.currentTime();

        final Memory memoryEnd = Memory.getCurrent();


        final double runningTime = overallEnd - overallStart;

        final Map<String, Result> results = new HashMap<>();
        int fails = 0;
        for (final SingleInvocationStatCalculator stat : stats.values()) {
            final Result r = stat.getResult();
            fails += r.getErrors().size();
            results.put(r.getName(), r);
        }

        return new StressResult(results, threadsNumber, cycleCount,
                fails, runningTime, memoryStart, memoryEnd);
    }

    public StressResult measure(final int threadsNumber,
                                final int cycleCount,
                                final Action action)
            throws InterruptedException {

        final ConcurrentMap<String, MultiInvocationStatCalculator> result =
                new ConcurrentHashMap<>(16, 0.75f, threadsNumber);

        final List<MeasureProvider> providers = new ArrayList<>();
        for (int i = 0; i < threadsNumber; i++) {
            for (int j = i * cycleCount; j < (i + 1) * cycleCount; j++) {
                providers.add(new MeasureProvider(this, i, j, threadsNumber, result));
            }
        }

        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadsNumber; i++) {
            final int k = i;
            threads.add(new Thread(() -> {
                for (int j = k * cycleCount; j < (k + 1) * cycleCount; j++) {
                    try {
                        action.doAction(providers.get(j));
                    } catch (final Exception e) {
                        // TODO: print skipped
                    }
                }
            }, "stress-worker-" + k));
        }

        final Memory memoryStart = Memory.getCurrent();
        final long overallStart = resolution.currentTime();
        threads.forEach(java.lang.Thread::start);
        for (final Thread thread : threads) {
            thread.join();
        }
        final long overallEnd = resolution.currentTime();
        final Memory memoryEnd = Memory.getCurrent();

        final double runtime = overallEnd - overallStart;

        int fails = 0;
        final Map<String, Result> results = new HashMap<>();
        for (final MultiInvocationStatCalculator stats : result.values()) {
            final Result r = stats.getResult();
            fails += r.getErrors().size();
            results.put(r.getName(), r);
        }

        return new StressResult(results, threadsNumber, cycleCount,
                fails, runtime, memoryStart, memoryEnd);
    }

    public StressResult measurePooled(final int threadsNumber,
                                      final int cycleCount,
                                      final int coordinateThreadsCount,
                                      final PooledAction action)
            throws InterruptedException {

        final LoadingCache<String, ExecutorService> threadPools =
                CacheBuilder.newBuilder()
                            .concurrencyLevel(threadsNumber)
                            .build(new PoolLoader(threadsNumber));

        final int concurrency = threadsNumber * coordinateThreadsCount;

        final LoadingCache<String, MultiInvocationStatCalculator> calcPool =
                CacheBuilder.newBuilder()
                            .concurrencyLevel(threadsNumber)
                            .build(new CacheLoader<String, MultiInvocationStatCalculator>() {
                                @Override
                                public MultiInvocationStatCalculator load(final String key) throws Exception {
                                    return new MultiInvocationStatCalculator(getResolution(), key,
                                                                             concurrency);
                                }
                            });

        final List<PooledMeasureProvider> providers = new ArrayList<>();
        for (int i = 0; i < threadsNumber; i++) {
            for (int j = i * cycleCount; j < (i + 1) * cycleCount; j++) {
                providers.add(new PooledMeasureProvider(i, j, concurrency, calcPool, threadPools));
            }
        }

        final int mul = threadsNumber / coordinateThreadsCount;
        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < coordinateThreadsCount; i++) {
            final int k = i;
            threads.add(new Thread(() -> {
                for (int j = k * mul * cycleCount; j < (k + 1) * cycleCount * mul; j++) {
                    try {
                        action.doAction(providers.get(j));
                    } catch (final Exception e) {
                        // TODO: print skipped
                    }
                }
            }, "stress-worker-" + k));
        }

        final Memory memoryStart = Memory.getCurrent();
        final long overallStart = resolution.currentTime();
        threads.forEach(java.lang.Thread::start);
        for (final Thread thread : threads) {
            thread.join();
        }
        for (final ExecutorService service : threadPools.asMap().values()) {
            try {
                service.shutdown();
                service.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
            } catch (final InterruptedException ignored) {
            }
        }
        final long overallEnd = resolution.currentTime();
        final Memory memoryEnd = Memory.getCurrent();

        final double runtime = overallEnd - overallStart;

        int fails = 0;
        final Map<String, Result> results = new HashMap<>();
        for (final MultiInvocationStatCalculator stats : calcPool.asMap().values()) {
            final Result r = stats.getResult();
            fails += r.getErrors().size();
            results.put(r.getName(), r);
        }

        return new StressResult(results, threadsNumber, cycleCount,
                fails, runtime, memoryStart, memoryEnd);
    }

    private static class PoolLoader extends CacheLoader<String, ExecutorService> {
        private final int threadsNumber;

        private PoolLoader(final int threadsNumber) {
            this.threadsNumber = threadsNumber;
        }

        @Override
        public ExecutorService load(final String key) throws Exception {
            final String nameFormat = "measure-pool-" + key + "-%d";
            final ThreadFactory threadFactory =
                    new ThreadFactoryBuilder().setNameFormat(nameFormat).build();
            return Executors.newFixedThreadPool(threadsNumber,
                    threadFactory);
        }
    }
}
