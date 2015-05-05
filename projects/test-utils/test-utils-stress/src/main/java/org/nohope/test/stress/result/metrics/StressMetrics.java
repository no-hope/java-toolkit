package org.nohope.test.stress.result.metrics;

import com.google.common.collect.Lists;
import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.MemoryManagerMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.management.ManagementFactory.*;

/**
 */
public final class StressMetrics {
    private static final OperatingSystemMXBean OS_BEAN = (OperatingSystemMXBean) getOperatingSystemMXBean();
    private static final List<GarbageCollectorMXBean> GC_BEANS = Lists.transform(getGarbageCollectorMXBeans(),
            GarbageCollectorMXBean.class::cast);

    private final Map<String, GcMetrics> gcMetrics = new HashMap<>();
    private final ProcessMetrics processMetrics;
    private final SystemMetrics systemMetrics;
    private final long timestamp;

    private StressMetrics(final Map<String, GcMetrics> gcMetrics, final ProcessMetrics processMetrics, final SystemMetrics systemMetrics) {
        this.gcMetrics.putAll(gcMetrics);
        this.processMetrics = processMetrics;
        this.systemMetrics = systemMetrics;
        this.timestamp = System.nanoTime();
    }

    public static StressMetrics get() {
        final Map<String, GcMetrics> gcStats = GC_BEANS.stream().collect(Collectors.toMap(MemoryManagerMXBean::getName,
                gc -> new GcMetrics(gc.getLastGcInfo(), gc.getCollectionCount(), gc.getCollectionTime())));
        final ProcessMetrics processStat = new ProcessMetrics(OS_BEAN.getProcessCpuLoad(), OS_BEAN.getProcessCpuTime() );
        final SystemMetrics systemStat = new SystemMetrics(OS_BEAN.getSystemCpuLoad(), OS_BEAN.getSystemLoadAverage());
        return new StressMetrics(gcStats, processStat, systemStat);
    }

    public Map<String, GcMetrics> getGcMetrics() {
        return gcMetrics;
    }

    public ProcessMetrics getProcessMetrics() {
        return processMetrics;
    }

    public SystemMetrics getSystemMetrics() {
        return systemMetrics;
    }

    public long getTimestampNanos() {
        return timestamp;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StressMetrics)) {
            return false;
        }

        final StressMetrics that = (StressMetrics) o;

        return timestamp == that.timestamp
            && gcMetrics.equals(that.gcMetrics)
            && processMetrics.equals(that.processMetrics);
    }

    @Override
    public int hashCode() {
        int result = gcMetrics.hashCode();
        result = 31 * result + processMetrics.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }
}
