package org.nohope.cassandra.util

import org.junit.Test
import org.nohope.cassandra.factory.CassandraSchemaException
import org.nohope.cassandra.validation.CassandraSchemaValidatorTableMustExists
import org.nohope.cassandra.factory.CassandraTools

import static org.mockito.Matchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

/**
 */
class CassandraSchemaValidatorTableMustExistsTest {

    @Test(expected = CassandraSchemaException.class)
    void throws_if_table_not_exits() {
        def cassandraTools = mock(CassandraTools.class)
        def validator = new CassandraSchemaValidatorTableMustExists();
        validator.setCassandraTools(cassandraTools)

        when(cassandraTools.isTableExists(eq("keyspace"), eq("table"))).thenReturn(false);
        validator.validateTableStatus("keyspace", "table")
    }

    @Test
    void not_throws_if_table_exists() {
        def cassandraTools = mock(CassandraTools.class)
        def validator = new CassandraSchemaValidatorTableMustExists();
        validator.setCassandraTools(cassandraTools)

        when(cassandraTools.isTableExists(eq("keyspace"), eq("table"))).thenReturn(true);
        validator.validateTableStatus("keyspace", "table")
    }
}
