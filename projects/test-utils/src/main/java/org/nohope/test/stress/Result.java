package org.nohope.test.stress;

import java.util.Map;
import java.util.TreeMap;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:19
*/
public class Result {
    private final double meanRequestTime;
    private final double throughput;
    private final long minTime;
    private final long maxTime;
    private final Map<Long, Long> requests = new TreeMap<>();
    private final double workerThrp;

    public Result(final Map<Long, Long> requests,
                  final double meanRequestTime,
                  final double throughput,
                  final double workerThrp, final long minTime,
                  final long maxTime) {
        this.meanRequestTime = meanRequestTime;
        this.throughput = throughput;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.requests.putAll(requests);
        this.workerThrp = workerThrp;
    }

    public double getMeanRequestTime() {
        return meanRequestTime;
    }

    public double getThroughput() {
        return throughput;
    }

    public long getMinTime() {
        return minTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public Map<Long, Long> getRequests() {
        return requests;
    }

    public double getWorkerThrp() {
        return workerThrp;
    }
}
