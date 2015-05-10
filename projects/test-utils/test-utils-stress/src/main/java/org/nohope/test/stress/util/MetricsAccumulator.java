package org.nohope.test.stress.util;

import org.nohope.test.stress.result.metrics.StressMetrics;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 */
public class MetricsAccumulator {
    private final List<StressMetrics> metrics = new ArrayList<>();
    private final ScheduledExecutorService scheduledThreadPoolExecutor = Executors.newSingleThreadScheduledExecutor();

    private final Duration period;

    public MetricsAccumulator(final Duration period) {
        this.period = period;
    }

    private void storeMetric() {
        metrics.add(StressMetrics.get());
    }

    public List<StressMetrics> getMetrics() {
        return Collections.unmodifiableList(metrics);
    }

    public void start() {
        scheduledThreadPoolExecutor.scheduleAtFixedRate(this::storeMetric, 0,
                period.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduledThreadPoolExecutor.shutdown();
        try {
            scheduledThreadPoolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
        } catch (final InterruptedException ignored) {
        }
    }
}
