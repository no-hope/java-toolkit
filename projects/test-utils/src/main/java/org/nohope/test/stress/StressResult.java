package org.nohope.test.stress;

import java.util.HashMap;
import java.util.Map;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:19
*/
public class StressResult {
    private final Map<String, IStressStat> results = new HashMap<>();
    private final long start;
    private final long end;
    private final double runtime;
    private final int fails;
    private final int measurements;

    public StressResult(final Map<String, ? extends IStressStat> stats,
                        final int measurements,
                        final int fails,
                        final long start,
                        final long end,
                        final double runtime) {
        this.start = start;
        this.end = end;
        this.runtime = runtime;
        this.fails = fails;
        this.measurements = measurements;
        results.putAll(stats);
    }

    public double getApproxThroughput() {
        return (measurements * 1.0 - fails) / runtime;
    }

    public Map<String, IStressStat> getResults() {
        return results;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public double getRuntime() {
        return runtime;
    }

    public int getFails() {
        return fails;
    }

    public int getMeasurements() {
        return measurements;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("===== Stress test result =====\n")
               .append("==============================\n");
        for (final IStressStat stats : results.values()) {
            builder.append(stats.toString());
        }

        return builder.append("==============================\n")
                      .append("Overall Running Time: ")
                      .append(runtime)
                      .append(", approx throughput: ")
                      .append(getApproxThroughput())
                      .toString();
    }
}
