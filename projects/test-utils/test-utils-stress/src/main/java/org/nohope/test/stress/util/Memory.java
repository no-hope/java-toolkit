package org.nohope.test.stress.util;

import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2014-03-18 12:15
 */
public class Memory {
    private final long maxMemory;
    private final long totalMemory;
    private final long freeMemory;


    private Memory(final long maxMemory, final long totalMemory, final long freeMemory) {
        this.maxMemory = maxMemory;
        this.totalMemory = totalMemory;
        this.freeMemory = freeMemory;
    }

    public static Memory getCurrent() {
        final Runtime runtime = Runtime.getRuntime();
        System.gc();
        return new Memory(runtime.maxMemory(), runtime.totalMemory(), runtime.freeMemory());
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    @Override
    public String toString() {
        return "max: " + byteCountToDisplaySize(maxMemory)
               + ", total: " + byteCountToDisplaySize(totalMemory)
               + ", free: " + byteCountToDisplaySize(freeMemory)
               + ", heap: " + byteCountToDisplaySize(totalMemory - freeMemory);
    }
}
