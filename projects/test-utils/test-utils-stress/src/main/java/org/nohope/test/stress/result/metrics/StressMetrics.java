package org.nohope.test.stress.result.metrics;

import com.google.common.collect.Lists;
import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.MemoryManagerMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.management.ManagementFactory.getGarbageCollectorMXBeans;
import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;

/**
 */
public final class StressMetrics {
    private static final OperatingSystemMXBean OS_BEAN = (OperatingSystemMXBean) getOperatingSystemMXBean();
    private static final List<GarbageCollectorMXBean> GC_BEANS = Lists.transform(getGarbageCollectorMXBeans(),
            GarbageCollectorMXBean.class::cast);

    private final Map<String, GcMetrics> gcMetrics = new HashMap<>();
    private final ProcessMetrics processMetrics;

    private StressMetrics(final Map<String, GcMetrics> gcMetrics, final ProcessMetrics processMetrics) {
        this.gcMetrics.putAll(gcMetrics);
        this.processMetrics = processMetrics;
    }

    public static StressMetrics get() {
        final Map<String, GcMetrics> gcStats = GC_BEANS.stream().collect(Collectors.toMap(MemoryManagerMXBean::getName,
                gc -> new GcMetrics(gc.getLastGcInfo(), gc.getCollectionCount(), gc.getCollectionTime())));
        final ProcessMetrics processStat = new ProcessMetrics(OS_BEAN.getProcessCpuLoad(), OS_BEAN.getProcessCpuTime());
        return new StressMetrics(gcStats, processStat);
    }

    public Map<String, GcMetrics> getGcMetrics() {
        return gcMetrics;
    }

    public ProcessMetrics getProcessMetrics() {
        return processMetrics;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final StressMetrics that = (StressMetrics) o;
        return gcMetrics.equals(that.gcMetrics)
            && processMetrics.equals(that.processMetrics);
    }

    @Override
    public int hashCode() {
        int result = gcMetrics.hashCode();
        result = 31 * result + processMetrics.hashCode();
        return result;
    }
}
