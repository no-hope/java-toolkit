package org.nohope.cassandra.factory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.ExponentialReconnectionPolicy;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.nohope.cassandra.ClusterCustomization;

/**
 * TODO: remove from main scope
 */
public final class ITHelpers {
    private ITHelpers() {
    }

    public static void dropKeyspace(CassandraFactory factory) {
        factory.getSession().execute("DROP KEYSPACE " + QueryBuilder.quote(factory.getKeyspace()));
    }

    public static void destroy(CassandraFactory factory) {
        Session session = factory.getSession();
        dropKeyspace(factory);
        session.close();
        session.getCluster().close();
    }

    public static CassandraFactory cassandraFactory() {
        String keySpace = "test_keyspace_" + RandomStringUtils.random(10, true, false);
        return cassandraFactory(keySpace);
    }

    public static CassandraFactory cassandraFactory(String keySpace) {
        final CassandraFactory factory = new CassandraFactory();
        final String envHost = System.getenv("CASSANDRA_TEST_HOST");
        final String propHost = System.getProperty("cassandra.nodes");
        final String host;

        if (StringUtils.isNotEmpty(propHost)) {
            host = propHost;
        } else if (StringUtils.isNotEmpty(envHost)) {
            host = envHost;
        } else {
            host = "localhost";
        }

        factory.setEndpoints(host);
        factory.setReplication("{'class':'SimpleStrategy', 'replication_factor':1}");
        factory.setKeyspace(keySpace);

        // map huge timeout for heavy integrational tests
        factory.setCustomization(new ClusterCustomization() {
            @Override
            public void customize(final Cluster.Builder builder) {
                builder.withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                       .withReconnectionPolicy(new ExponentialReconnectionPolicy(200, 20000));
            }
        });

        factory.init();
        return factory;
    }

    public static void withFactory(final FactoryHandler handler) throws Exception {
        CassandraFactory factory = null;
        try {
            factory = cassandraFactory();
            handler.handle(factory);
        } finally {
            if (factory != null) {
                destroy(factory);
            }
        }
    }

    public interface FactoryHandler {
        void handle(CassandraFactory f) throws Exception;
    }
}
