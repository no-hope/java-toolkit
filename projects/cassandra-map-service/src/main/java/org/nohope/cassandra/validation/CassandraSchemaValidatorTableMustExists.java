package org.nohope.cassandra.validation;

import org.nohope.cassandra.factory.CassandraSchemaException;
import org.nohope.cassandra.factory.CassandraTools;

/**
 */
public class CassandraSchemaValidatorTableMustExists implements CassandraSchemaValidator {
    private CassandraTools cassandraTools;

    public void setCassandraTools(final CassandraTools cassandraTools) {
        this.cassandraTools = cassandraTools;
    }

    @Override
    public void validateTableStatus(final String keySpace, final String tableName)
            throws CassandraSchemaException {
        if (!cassandraTools.isTableExists(keySpace, tableName)) {
            throw new CassandraSchemaException(
                    String.format("Table [%s] in keyspace [%s] not exists",
                            tableName,
                            keySpace));
        }
    }

    @Override
    public boolean shouldCreateTable(final String keySpace, final String tableName) {
        return false;
    }

    @Override
    public boolean pingCassandra() {
        return this.cassandraTools.pingCassandra();
    }
}
