package org.nohope.test.stress.result;

import org.apache.commons.lang3.StringUtils;
import org.nohope.test.stress.util.Memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.nohope.test.stress.util.TimeUtils.*;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:19
*/
public class StressResult {
    private final Map<String, ActionResult> results;
    private final double runtime;
    private final int totalExceptionsCount;
    private final int threadsCount;
    private final int cycleCount;
    private final Memory memoryStart;
    private final Memory memoryEnd;
    private final List<Exception> allExceptions;
    private final List<StressMetrics> metrics;


    public StressResult(final Map<String, ActionResult> stats,
                        final int threadsCount,
                        final int cycleCount,
                        final double runtime,
                        final List<StressMetrics> metrics,
                        final Memory memoryStart,
                        final Memory memoryEnd) {
        this.runtime = runtime;
        this.allExceptions = stats.entrySet().stream()
                             .flatMap(entry -> entry.getValue().getErrors().stream())
                             .collect(Collectors.toList());
        this.totalExceptionsCount = allExceptions.size();
        this.threadsCount = threadsCount;
        this.cycleCount = cycleCount;
        results = new HashMap<>(stats.size());
        this.results.putAll(stats);

        this.metrics = new ArrayList<>(metrics.size());
        this.metrics.addAll(metrics);
        this.memoryStart = memoryStart;
        this.memoryEnd = memoryEnd;
    }


    public List<StressMetrics> getMetrics() {
        return metrics;
    }


    /**
     * @return per test results
     */
    public Map<String, ActionResult> getResults() {
        return results;
    }

    /**
     * @return approximate overall throughput in op/sec
     */
    public double getApproxThroughput() {
        return (threadsCount * cycleCount * 1.0 - totalExceptionsCount) / runtime;
    }

    /**
     * @return overall running time in milliseconds
     */
    public double getRuntime() {
        return runtime;
    }

    /**
     * @return overall exceptions count
     */
    public int getTotalExceptionsCount() {
        return totalExceptionsCount;
    }

    public Memory getMemoryStart() {
        return memoryStart;
    }

    public Memory getMemoryEnd() {
        return memoryEnd;
    }


    public int getThreadsCount() {
        return threadsCount;
    }


    public int getCycleCount() {
        return cycleCount;
    }


    public List<Exception> getAllExceptions() {
        return Collections.unmodifiableList(allExceptions);
    }


    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        final String separator = StringUtils.rightPad("", 50, '=');
        builder.append(StringUtils.rightPad("====== Stress test result ", 50, '='))
               .append('\n')
               .append(pad("Threads: "))
               .append(threadsCount)
               .append('\n')
               .append(pad("Cycles: "))
               .append(cycleCount)
               .append('\n')
               .append(separator)
               .append('\n');

        // sorted output
        final Iterable<String> keys = new TreeSet<>(results.keySet());
        for (final String key : keys) {
            builder.append(results.get(key));
        }

        return builder.append(separator)
                      .append('\n')
                      .append(pad("Total error count:"))
                      .append(totalExceptionsCount)
                      .append('\n')
                      .append(pad("Total running time:"))
                      .append(String.format("%.3f", timeTo(runtime, SECONDS)))
                      .append(" sec\n")
                      .append(pad("Approximate throughput:"))
                      .append(String.format("%.3e", throughputTo(getApproxThroughput(), SECONDS)))
                      .append(" op/sec")
                      .append('\n')
                      .append(pad("Memory usage before test: "))
                      .append(memoryStart)
                      .append('\n')
                      .append(pad("Memory usage after test: "))
                      .append(memoryEnd)
                      .append('\n')
                      .toString();
    }

    private static String pad(final String str) {
        final int padSize = 30;
        return StringUtils.rightPad(str, padSize, '.');
    }
}
