package org.nohope.cassandra.validation;

import org.nohope.cassandra.factory.CassandraSchemaException;
import org.nohope.cassandra.factory.CassandraTools;

/**
 */
public class CassandraSchemaValidatorTableExistensceDoNotCare implements CassandraSchemaValidator {
    private CassandraTools cassandraTools;

    public void setCassandraTools(final CassandraTools cassandraTools) {
        this.cassandraTools = cassandraTools;
    }

    @Override
    public void validateTableStatus(final String keySpace, final String tableName) throws CassandraSchemaException {
    }

    @Override
    public boolean shouldCreateTable(final String keySpace, final String tableName) {
        return true;
    }

    @Override
    public boolean pingCassandra() {
        return this.cassandraTools.pingCassandra();
    }
}
