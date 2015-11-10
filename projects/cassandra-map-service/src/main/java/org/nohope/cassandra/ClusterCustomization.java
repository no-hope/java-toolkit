package org.nohope.cassandra;

import com.datastax.driver.core.Cluster;

/**
 * @since 2015-01-29 01:33
 */
public interface ClusterCustomization {
    ClusterCustomization NONE = new ClusterCustomization() {
        @Override
        public void customize(final Cluster.Builder builder) {
        }
    };

    void customize(final Cluster.Builder builder);
}
