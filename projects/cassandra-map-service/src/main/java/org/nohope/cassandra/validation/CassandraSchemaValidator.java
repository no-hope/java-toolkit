package org.nohope.cassandra.validation;

import org.nohope.cassandra.factory.CassandraSchemaException;

/**
 */
public interface CassandraSchemaValidator {
    void validateTableStatus(String keySpace, String tableName) throws CassandraSchemaException;

    boolean shouldCreateTable(String keySpace, String tableName);

    boolean pingCassandra();
}
