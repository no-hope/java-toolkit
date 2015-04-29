package org.nohope.test.stress.result.metrics;

import com.sun.management.GcInfo;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2015-04-29 22:09
 */
public final class GcMetrics {
    private final GcInfo gcInfo;
    private final long collectionCount;
    private final long collectionTime;

    public GcMetrics(final GcInfo gcInfo, final long collectionCount, final long collectionTime) {
        this.gcInfo = gcInfo;
        this.collectionCount = collectionCount;
        this.collectionTime = collectionTime;
    }

    public GcInfo getGcInfo() {
        return gcInfo;
    }

    public long getCollectionCount() {
        return collectionCount;
    }

    public long getCollectionTime() {
        return collectionTime;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final GcMetrics gcMetrics = (GcMetrics) o;
        return collectionCount == gcMetrics.collectionCount
               && collectionTime == gcMetrics.collectionTime
               && gcInfo.equals(gcMetrics.gcInfo);
    }

    @Override
    public int hashCode() {
        int result = gcInfo.hashCode();
        result = 31 * result + (int) (collectionCount ^ (collectionCount >>> 32));
        result = 31 * result + (int) (collectionTime ^ (collectionTime >>> 32));
        return result;
    }
}
