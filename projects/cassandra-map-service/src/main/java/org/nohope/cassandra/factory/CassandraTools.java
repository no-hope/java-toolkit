package org.nohope.cassandra.factory;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;

/**
 */
public class CassandraTools {
    private PreparedStatementNamedParameters findAtLeastOneTableInKeySpace;
    private PreparedStatementNamedParameters findKeySpace;
    private PreparedStatementNamedParameters pingRequest;
    private Session cassandraSession;

    public void setCassandraSession(Session cassandraSession) {
        this.cassandraSession = cassandraSession;
    }

    public void init() {
        findAtLeastOneTableInKeySpace = new PreparedStatementNamedParameters(cassandraSession,
                "SELECT COUNT(*) from system.schema_columns WHERE keyspace_name = $keyspace_name AND columnfamily_name= $table_name LIMIT 1;");
        findKeySpace = new PreparedStatementNamedParameters(cassandraSession,
                "SELECT COUNT(*) from system.schema_keyspaces WHERE keyspace_name= $keyspace_name LIMIT 1");
        pingRequest = new PreparedStatementNamedParameters(cassandraSession,
                "SELECT COUNT(*) from system.schema_keyspaces LIMIT 1;");
    }

    public boolean isTableExists(final String keySpace, final String tableName) {
        final Row row = findAtLeastOneTableInKeySpace.execute(
                ImmutableMap.of(
                        "keyspace_name", keySpace,
                        "table_name", tableName
                )).one();

        return row.getLong("Count") >= 1;
    }

    public boolean pingCassandra() {
        final Row row = pingRequest.execute(Collections.<String, Object> emptyMap()).one();
        return row.getLong("Count") >= 1;
    }

    boolean isKeySpaceExists(final String keySpace) {
        final Row row = findKeySpace.execute(ImmutableMap.of("keyspace_name", keySpace)).one();
        if (null == row) {
            throw new RuntimeException("Unexpected result of querying cassandra embedded by default table");
        }
        return row.getLong("Count") >= 1;
    }
}
