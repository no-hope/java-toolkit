package org.nohope.cassandra

import com.datastax.driver.core.Session
import org.nohope.cassandra.factory.CassandraFactory
import org.nohope.cassandra.factory.ITHelpers

/**
 * @since 2014-04-23 17:56
 */
trait CassandraITMixin {
    CassandraFactory factory
    Session session

    void setUpCassandra() {
        factory = ITHelpers.cassandraFactory()
        session = factory.getSession()
    }

    void cleanUpCassandra() {
        ITHelpers.destroy(factory)
    }
}
