package org.nohope.test.stress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Map.Entry;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:19
*/
public class Result {
    private final Map<Integer, List<Double>> timesPerThread = new HashMap<>();
    private final Map<Class, List<Exception>> errorStats = new HashMap<>();
    private final Map<Class, List<Throwable>> rootErrorStats = new HashMap<>();

    private final String name;

    private final double meanRequestTime;
    private final double throughput;
    private final double minTime;
    private final double maxTime;
    private final double workerThroughput;
    private final double totalDeltaSeconds;

    public Result(final String name,
                  final Map<Integer, List<Double>> timesPerThread,
                  final Map<Class, List<Exception>> errorStats,
                  final ConcurrentHashMap<Class, List<Throwable>> rootErrorStats,
                  final double totalDeltaSeconds,
                  final double meanRequestTime,
                  final double throughput,
                  final double workerThroughput,
                  final double minTime,
                  final double maxTime) {
        this.name = name;
        this.meanRequestTime = meanRequestTime;
        this.throughput = throughput;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.workerThroughput = workerThroughput;
        this.totalDeltaSeconds = totalDeltaSeconds;
        this.timesPerThread.putAll(timesPerThread);
        this.errorStats.putAll(errorStats);
        this.rootErrorStats.putAll(rootErrorStats);
    }

    /**
     * @return test scenario name
     */
    public String getName() {
        return name;
    }

    /**
     * @return average request time in milliseconds
     */
    public double getMeanTime() {
        return meanRequestTime;
    }

    /**
     * @return minimum request time in milliseconds
     */
    public double getMinTime() {
        return minTime;
    }

    /**
     * @return maximum request time in milliseconds
     */
    public double getMaxTime() {
        return maxTime;
    }

    /**
     * @return overall op/sec
     */
    public double getThroughput() {
        return throughput;
    }

    /**
     * @return op/sec per thread
     */
    public double getWorkerThroughput() {
        return workerThroughput;
    }

    /**
     * @return get pure running time in seconds
     */
    public double getRuntime() {
        return totalDeltaSeconds;
    }

    /**
     * @return in milliseconds
     */
    public Map<Integer, List<Double>> getPerThreadRuntimes() {
        return timesPerThread;
    }

    /**
     * @return list of all exceptions thrown during test scenario
     */
    public List<Exception> getErrors() {
        final List<Exception> result = new ArrayList<>();
        for (final List<Exception> exceptions : errorStats.values()) {
            result.addAll(exceptions);
        }
        return result;
    }

    /**
     * @return list of all exceptions split by topmost exception class
     */
    public Map<Class, List<Exception>> getErrorsPerClass() {
        return errorStats;
    }

    /**
     * @return list of all running times of each thread
     */
    public final List<Double> getRuntimes() {
        final List<Double> times = new ArrayList<>();
        for (final List<Double> e : timesPerThread.values()) {
            times.addAll(e);
        }
        return times;
    }

    @Override
    public final String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("----- Stats for (name: ")
               .append(name)
               .append(") -----\n");

        builder.append("Min operation time: ")
               .append(String.format("%.3f", minTime))
               .append(" ms")
               .append('\n');

        builder.append("Max operation time: ")
               .append(String.format("%.3f", maxTime))
               .append(" ms")
               .append('\n');

        builder.append("Avg operation time: ")
               .append(String.format("%.3f", meanRequestTime))
               .append(" ms")
               .append('\n');

        int fails = 0;
        for (final List<Exception> exceptions : errorStats.values()) {
            fails += exceptions.size();
        }

        builder.append("Errors count: ")
               .append(fails)
               .append('\n');

        for (final Entry<Class, List<Exception>> e : errorStats.entrySet()) {
            builder.append("| ")
                   .append(e.getKey().getName())
                   .append(" happened ")
                   .append(e.getValue().size())
                   .append(" times")
                   .append('\n');
        }

        builder.append("Roots:\n");

        for (final Entry<Class, List<Throwable>> e : rootErrorStats.entrySet()) {
            builder.append("| ")
                   .append(e.getKey().getName())
                   .append(" happened ")
                   .append(e.getValue().size())
                   .append(" times")
                   .append('\n');
        }

        int times = 0;
        for (final List<Double> e : timesPerThread.values()) {
            times += e.size();
        }

        builder.append("Operations: ")
               .append(times)
               .append('\n');

        builder.append("Running time: ")
               .append(String.format("%.3f", totalDeltaSeconds))
               .append(" sec\n");

        builder.append("Running time per thread: ")
               .append(String.format("%.3f", totalDeltaSeconds / timesPerThread.size()))
               .append(" sec\n");

        builder.append("Avg thread throughput: ")
               .append(String.format("%e", workerThroughput))
               .append(" op/sec")
               .append('\n');

        builder.append("Avg throughput: ")
               .append(String.format("%e",throughput))
               .append(" op/sec")
               .append('\n');

        return builder.toString();
    }
}
