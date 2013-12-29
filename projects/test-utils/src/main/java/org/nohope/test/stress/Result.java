package org.nohope.test.stress;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:19
*/
public class Result {
    private final double meanRequestTime;
    private final double throughput;
    private final double minTime;
    private final double maxTime;
    private final double workerThroughput;

    public Result(final double meanRequestTime,
                  final double throughput,
                  final double workerThroughput,
                  final double minTime,
                  final double maxTime) {
        this.meanRequestTime = meanRequestTime;
        this.throughput = throughput;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.workerThroughput = workerThroughput;
    }

    /**
     * @return average request time in milliseconds
     */
    public double getMeanRequestTime() {
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
}
