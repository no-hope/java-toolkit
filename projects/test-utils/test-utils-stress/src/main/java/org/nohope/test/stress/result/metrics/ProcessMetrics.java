package org.nohope.test.stress.result.metrics;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2015-04-29 22:10
 */
public final class ProcessMetrics {
    private final double processCpuLoad;
    private final long processCpuTime;

    public ProcessMetrics(final double processCpuLoad, final long processCpuTime) {
        this.processCpuLoad = processCpuLoad;
        this.processCpuTime = processCpuTime;
    }

    public double getProcessCpuLoad() {
        return processCpuLoad;
    }

    public long getProcessCpuTime() {
        return processCpuTime;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ProcessMetrics that = (ProcessMetrics) o;
        return Double.compare(that.processCpuLoad, processCpuLoad) == 0
               && processCpuTime == that.processCpuTime;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(Double.doubleToLongBits(processCpuLoad));
        result = 31 * result + Long.hashCode(processCpuTime);
        return result;
    }
}
