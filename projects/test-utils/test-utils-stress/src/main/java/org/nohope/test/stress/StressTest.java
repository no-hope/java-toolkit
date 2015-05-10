package org.nohope.test.stress;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.nohope.test.stress.actions.Scenario;
import org.nohope.test.stress.functors.Call;
import org.nohope.test.stress.functors.Get;

import java.time.Duration;
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
public final class StressTest {
    private final Builder builder;

    private StressTest(final Builder builder) {
        this.builder = builder;
    }

    public static StressTest instance() {
        return new Builder().build();
    }

    /**
     * This method runs {@code threadsNumber}, each performing {@code cycleCount}
     * invocations of {@code scenario}
     */
    public PreparedStressTest prepare(final int threadsNumber, final int cycleCount,
                                      final Scenario<MeasureProvider> scenario) {
        final Map<String, ActionStatsAccumulator> accumulators = new ConcurrentHashMap<>(16, 0.75f, threadsNumber);
        final Function<String, ActionStatsAccumulator> accumulatorLoader = ActionStatsAccumulator::new;
        final Function<String, ActionStatsAccumulator> accumulatorGetter =
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
                        scenario.doAction(providers.get(j));
                    } catch (final Exception e) {
                        // TODO: print skipped
                    }
                }
            }, "stress-worker-" + k));
        }

        return new PreparedStressTest(threadsNumber, cycleCount,
                Collections.emptyList(), accumulators.values(), threads,
                builder.metricsInterval);
    }

    /**
     * This method runs {@code coordinatorThreadsCount} threads, performing
     * {@code cyclesPerCoordinatorCount} invocations of {@code scenario}
     * <p />
     *
     * Coordinator invokes each kind {@link MeasureProvider#get(String, Get)} or
     * {@link MeasureProvider#call(String, Call)} in separate thread pools sized to
     * {@code actionThreadPoolSize}.
     */
    public PreparedStressTest prepare(final int coordinatorThreadsCount,
                                      final int cyclesPerCoordinatorCount,
                                      final int actionThreadPoolSize,
                                      final Scenario<PooledMeasureProvider> scenario) {
        final Map<String, ExecutorService> executors = new ConcurrentHashMap<>(16, 0.75f, coordinatorThreadsCount);

        final Function<String, ExecutorService> executorLoader = name -> {
            final String nameFormat = "prepare-pool-" + name + "-%d";
            final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(nameFormat).build();
            return Executors.newFixedThreadPool(actionThreadPoolSize, threadFactory);
        };
        final Function<String, ExecutorService> executorGetter =
                name -> executors.computeIfAbsent(name, executorLoader);

        final Map<String, ActionStatsAccumulator> accumulators =
                new ConcurrentHashMap<>(16, 0.75f, actionThreadPoolSize * coordinatorThreadsCount);

        final Function<String, ActionStatsAccumulator> accumulatorLoader = ActionStatsAccumulator::new;
        final Function<String, ActionStatsAccumulator> accumulatorGetter =
                name -> accumulators.computeIfAbsent(name, accumulatorLoader);

        final int cycleCount = coordinatorThreadsCount * cyclesPerCoordinatorCount;

        final List<PooledMeasureProvider> providers = new ArrayList<>(cycleCount);
        for (int i = 0; i < actionThreadPoolSize; i++) {
            for (int j = i * cyclesPerCoordinatorCount; j < (i + 1) * cyclesPerCoordinatorCount; j++) {
                providers.add(new PooledMeasureProvider(i, j, executorGetter, accumulatorGetter));
            }
        }

        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < coordinatorThreadsCount; i++) {
            final int k = i;
            threads.add(new Thread(() -> {
                for (int j = k * cyclesPerCoordinatorCount; j < (k + 1) * cyclesPerCoordinatorCount; j++) {
                    try {
                        scenario.doAction(providers.get(j));
                    } catch (final Exception e) {
                        // TODO: print skipped
                    }
                }
            }, "stress-worker-" + k));
        }

        return new PreparedStressTest(actionThreadPoolSize, cycleCount,
                executors.values(), accumulators.values(),
                threads, builder.metricsInterval);
    }

    public static class Builder {
        private Duration metricsInterval = Duration.ofSeconds(2);

        public Builder metricsInterval(Duration metricsInterval) {
            this.metricsInterval = metricsInterval;
            return this;
        }

        public StressTest build() {
            return new StressTest(this);
        }
    }
}
