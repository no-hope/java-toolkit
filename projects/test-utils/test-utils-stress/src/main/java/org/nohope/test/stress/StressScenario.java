package org.nohope.test.stress;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.nohope.test.stress.actions.Action;
import org.nohope.test.stress.actions.NamedAction;
import org.nohope.test.stress.actions.PooledAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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

    public PreparedMeasurement prepare(final int threadsNumber,
                                final int cycleCount,
                                final NamedAction... actions)
            throws InterruptedException {
        final ConcurrentMap<String, StatAccumulator> result =
                new ConcurrentHashMap<>(16, 0.75f, threadsNumber);

        final List<MeasureProvider> providers = new ArrayList<>(threadsNumber * cycleCount);
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
                    for (NamedAction action : actions) {
                        try {
                            final MeasureProvider provider = providers.get(j);
                            provider.call(action.getName(), () -> action.doAction(provider));
                        } catch (final Exception e) {
                            // TODO: print skipped
                        }
                    }
                }
            }, "stress-worker-" + k));
        }

        return new PreparedMeasurement(resolution, threadsNumber, cycleCount, Collections.emptyList(), result.values(), threads);
    }

    public PreparedMeasurement prepare(final int threadsNumber,
                                final int cycleCount,
                                final Action action)
            throws InterruptedException {

        final ConcurrentMap<String, StatAccumulator> result =
                new ConcurrentHashMap<>(16, 0.75f, threadsNumber);

        final List<MeasureProvider> providers = new ArrayList<>(threadsNumber * cycleCount);
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
        return new PreparedMeasurement(resolution, threadsNumber, cycleCount, Collections.emptyList(), result.values(), threads);

    }

    public PreparedMeasurement prepare(final int threadsPerCoordinator,
                                final int cycleCount,
                                final int coordinateThreadsCount,
                                final PooledAction action)
            throws InterruptedException {

        final int concurrency = threadsPerCoordinator * coordinateThreadsCount;

        final LoadingCache<String, ExecutorService> threadPools =
                CacheBuilder.newBuilder()
                            .concurrencyLevel(concurrency)
                            .build(new PoolLoader(threadsPerCoordinator));


        final LoadingCache<String, StatAccumulator> calcPool =
                CacheBuilder.newBuilder()
                            .concurrencyLevel(threadsPerCoordinator)
                            .build(new CacheLoader<String, StatAccumulator>() {
                                @Override
                                public StatAccumulator load(final String key) throws Exception {
                                    return new StatAccumulator(getResolution(), key,
                                                               concurrency);
                                }
                            });

        final List<PooledMeasureProvider> providers = new ArrayList<>(threadsPerCoordinator * cycleCount);
        for (int i = 0; i < threadsPerCoordinator; i++) {
            for (int j = i * cycleCount; j < (i + 1) * cycleCount; j++) {
                providers.add(new PooledMeasureProvider(i, j, concurrency, calcPool, threadPools));
            }
        }

        final int mul = threadsPerCoordinator / coordinateThreadsCount;
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

        return new PreparedMeasurement(resolution, threadsPerCoordinator, cycleCount, threadPools.asMap().values(),
                                                   calcPool.asMap().values(), threads);
    }





    private static class PoolLoader extends CacheLoader<String, ExecutorService> {
        private final int threadsNumber;

        private PoolLoader(final int threadsNumber) {
            this.threadsNumber = threadsNumber;
        }

        @Override
        public ExecutorService load(final String key) throws Exception {
            final String nameFormat = "prepare-pool-" + key + "-%d";
            final ThreadFactory threadFactory =
                    new ThreadFactoryBuilder().setNameFormat(nameFormat).build();
            return Executors.newFixedThreadPool(threadsNumber,
                    threadFactory);
        }
    }
}
