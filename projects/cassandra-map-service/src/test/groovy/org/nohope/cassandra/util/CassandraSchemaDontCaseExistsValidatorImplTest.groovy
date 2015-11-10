package org.nohope.cassandra.util

import org.junit.Test
import org.nohope.cassandra.validation.CassandraSchemaValidatorTableExistensceDoNotCare
import org.nohope.cassandra.factory.CassandraTools

import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

/**
 */
class CassandraSchemaDontCaseExistsValidatorImplTest {

    @Test
    void throws_if_table_exits() {
        def cassandraTools = mock(CassandraTools.class)
        def validator = new CassandraSchemaValidatorTableExistensceDoNotCare();
        validator.setCassandraTools(cassandraTools)

        when(cassandraTools.isTableExists(eq("keyspace"), eq("table"))).thenReturn(true);
        validator.validateTableStatus("keyspace", "table")
    }

    @Test
    void not_throws_if_table_not_exists() {
        def cassandraTools = mock(CassandraTools.class)
        def validator = new CassandraSchemaValidatorTableExistensceDoNotCare();
        validator.setCassandraTools(cassandraTools)

        when(cassandraTools.isTableExists(eq("keyspace"), eq("table"))).thenReturn(false);
        validator.validateTableStatus("keyspace", "table")
        assertThat validator.shouldCreateTable("keyspace", "table"), equalTo(true)
    }
}
