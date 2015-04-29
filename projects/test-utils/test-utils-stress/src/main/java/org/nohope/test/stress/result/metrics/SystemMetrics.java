package org.nohope.test.stress.result.metrics;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2015-04-29 22:10
 */
public final class SystemMetrics {
    private final double systemLoadAverage;
    private final double systemCpuTime;

    public SystemMetrics(final double systemLoadAverage, final double systemCpuTime) {
        this.systemLoadAverage = systemLoadAverage;
        this.systemCpuTime = systemCpuTime;
    }

    public double getSystemLoadAverage() {
        return systemLoadAverage;
    }

    public double getSystemCpuTime() {
        return systemCpuTime;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SystemMetrics)) {
            return false;
        }

        final SystemMetrics that = (SystemMetrics) o;

        return Double.compare(that.systemLoadAverage, systemLoadAverage) == 0 && Double
                .compare(that.systemCpuTime, systemCpuTime) == 0;

    }


    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(systemLoadAverage);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(systemCpuTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
