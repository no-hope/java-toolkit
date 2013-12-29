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
    private final int threadsCount;
    private final int cycleCount;

    public StressResult(final Map<String, ? extends IStressStat> stats,
                        final int threadsCount,
                        final int cycleCount,
                        final int fails,
                        final long start,
                        final long end,
                        final double runtime) {
        this.start = start;
        this.end = end;
        this.runtime = runtime;
        this.fails = fails;
        this.threadsCount = threadsCount;
        this.cycleCount = cycleCount;
        this.results.putAll(stats);
    }

    public double getApproxThroughput() {
        return (threadsCount * cycleCount * 1.0 - fails) / runtime;
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

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("===== Stress test result =====\n")
               .append("Threads: ")
               .append(threadsCount)
               .append("\nCycles: ")
               .append(cycleCount)
               .append("\n==============================\n");
        for (final IStressStat stats : results.values()) {
            builder.append(stats.toString());
        }
        return builder.append("==============================\n")
                      .append("Overall Running Time: ")
                      .append(runtime)
                      .append(" sec\nApprox throughput: ")
                      .append(getApproxThroughput())
                      .append(" op/sec")
                      .toString();
    }
}
