package org.nohope.test.stress;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.nohope.test.stress.actions.Action;
import org.nohope.test.stress.actions.NamedAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-12-26 21:39
 */
public final class StressScenario {
    private StressScenario() {
    }

    public static PreparedMeasurement prepare(final int threadsNumber,
                                              final int cycleCount,
                                              final NamedAction... actions) {
        final Map<String, StatAccumulator> accumulators = new ConcurrentHashMap<>(16, 0.75f, threadsNumber);
        final Function<String, StatAccumulator> accumulatorLoader = StatAccumulator::new;
        final Function<String, StatAccumulator> accumulatorGetter =
                name -> accumulators.computeIfAbsent(name, accumulatorLoader);

        final List<MeasureProvider> providers = new ArrayList<>(threadsNumber * cycleCount);
        for (int i = 0; i < threadsNumber; i++) {
            for (int j = i * cycleCount; j < (i + 1) * cycleCount; j++) {
                providers.add(new MeasureProvider(i, j, accumulatorGetter));
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

        return new PreparedMeasurement(threadsNumber, cycleCount, Collections.emptyList(),
                accumulators.values(), threads);
    }

    public static PreparedMeasurement prepare(final int threadsNumber, final int cycleCount,
                                              final Action<MeasureProvider> action) {
        final Map<String, StatAccumulator> accumulators = new ConcurrentHashMap<>(16, 0.75f, threadsNumber);
        final Function<String, StatAccumulator> accumulatorLoader = StatAccumulator::new;
        final Function<String, StatAccumulator> accumulatorGetter =
                name -> accumulators.computeIfAbsent(name, accumulatorLoader);

        final List<MeasureProvider> providers = new ArrayList<>(threadsNumber * cycleCount);
        for (int i = 0; i < threadsNumber; i++) {
            for (int j = i * cycleCount; j < (i + 1) * cycleCount; j++) {
                providers.add(new MeasureProvider(i, j, accumulatorGetter));
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

        return new PreparedMeasurement(threadsNumber, cycleCount,
                Collections.emptyList(), accumulators.values(), threads);
    }

    public static PreparedMeasurement prepare(final int threadsPerCoordinator,
                                              final int cycleCount,
                                              final int coordinateThreadsCount,
                                              final Action<PooledMeasureProvider> action) {
        final Map<String, ExecutorService> executors = new ConcurrentHashMap<>(16, 0.75f, threadsPerCoordinator);
        final Function<String, ExecutorService> executorLoader = name -> {
            final String nameFormat = "prepare-pool-" + name + "-%d";
            final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(nameFormat).build();
            return Executors.newFixedThreadPool(threadsPerCoordinator, threadFactory);
        };
        final Function<String, ExecutorService> executorGetter =
                name -> executors.computeIfAbsent(name, executorLoader);

        final Map<String, StatAccumulator> accumulators =
                new ConcurrentHashMap<>(16, 0.75f, threadsPerCoordinator * coordinateThreadsCount);
        final Function<String, StatAccumulator> accumulatorLoader = StatAccumulator::new;
        final Function<String, StatAccumulator> accumulatorGetter =
                name -> accumulators.computeIfAbsent(name, accumulatorLoader);

        final List<PooledMeasureProvider> providers = new ArrayList<>(threadsPerCoordinator * cycleCount);
        for (int i = 0; i < threadsPerCoordinator; i++) {
            for (int j = i * cycleCount; j < (i + 1) * cycleCount; j++) {
                providers.add(new PooledMeasureProvider(i, j, executorGetter, accumulatorGetter));
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

        return new PreparedMeasurement(threadsPerCoordinator, cycleCount,
                executors.values(),
                accumulators.values(),
                threads);
    }
}
