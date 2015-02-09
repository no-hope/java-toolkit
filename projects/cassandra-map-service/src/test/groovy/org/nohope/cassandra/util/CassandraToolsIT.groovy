package org.nohope.cassandra.util

import com.datastax.driver.core.querybuilder.QueryBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.nohope.cassandra.CassandraITMixin
import org.nohope.cassandra.factory.CassandraTools

import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.MatcherAssert.assertThat

/**
 */
class CassandraToolsIT implements CassandraITMixin {

    private CassandraTools tools;

    @Before
    void setUp() {
        setUpCassandra()
        tools = new CassandraTools();
        tools.setCassandraSession(session)
        tools.init()
    }

    @After
    void cleanUp() {
        cleanUpCassandra()
    }

    @Test
    void check_key_space_exists() {
        assertThat tools.isKeySpaceExists(factory.keyspace), equalTo(true)
    }

    @Test
    void check_key_space_not_exists() {
        assertThat tools.isKeySpaceExists("not_found"), equalTo(false)
    }

    @Test
    void check_table_exists() {
        session.execute("CREATE TABLE test (id ascii PRIMARY KEY, data blob);");
        assertThat tools.isTableExists(factory.keyspace, "test"), equalTo(true)
    }

    @Test
    void check_table_exists_keyspace() {
        session.execute("CREATE TABLE "
                + QueryBuilder.quote(factory.keyspace)
                + ".test (id ascii PRIMARY KEY, data blob);");
        assertThat tools.isTableExists(factory.keyspace, "test"), equalTo(true)
    }

    @Test
    void check_table_not_exists() {
        assertThat tools.isTableExists(factory.keyspace, "not_found"), equalTo(false)
    }
}
