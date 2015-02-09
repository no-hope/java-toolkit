package org.nohope.cassandra

import org.junit.Test
import org.nohope.cassandra.factory.PreparedStatementNamedParameters

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.core.IsEqual.equalTo

/**
 */
class QueryWithNamedParametersTest {

    @Test
    void in_the_middle_is_correctly_mapped_to_position() {
        def query = 'something $named_parameter something else'
        def method = PreparedStatementNamedParameters.metaClass.getStaticMetaMethod("createPositionsToNames", String.class)
        def positionsToNames = method.invoke(QueryWithNamedParametersTest.class, query)
        assertThat positionsToNames, equalTo(["named_parameter"])
    }

    @Test
    void in_the_middle_is_correctly_converted_into_plain_old() {
        def query = 'something $named_parameter something else'
        def method = PreparedStatementNamedParameters.metaClass.getStaticMetaMethod("convertToSimpleQuery", String.class)
        def simpleQuery = method.invoke(QueryWithNamedParametersTest.class, query)
        assertThat simpleQuery, equalTo("something ? something else")
    }

    @Test
    void in_the_middle_escaped_is_correctly_mapped_to_position() {
        def query = 'something \\$named_parameter something else'
        def method = PreparedStatementNamedParameters.metaClass.getStaticMetaMethod("createPositionsToNames", String.class)
        def positionsToNames = method.invoke(QueryWithNamedParametersTest.class, query)
        assertThat positionsToNames, equalTo([])
    }

    @Test
    void in_the_middle_escaped_is_correctly_converted_into_plain_old() {
        def query = 'something \\$named_parameter something else'
        def method = PreparedStatementNamedParameters.metaClass.getStaticMetaMethod("convertToSimpleQuery", String.class)
        def simpleQuery = method.invoke(QueryWithNamedParametersTest.class, query)
        assertThat simpleQuery, equalTo('something $named_parameter something else')
    }

    @Test
    void in_the_middle_escaped_and_not_is_correctly_mapped_to_position() {
        def query = 'something \\$named_parameter $another something else'
        def method = PreparedStatementNamedParameters.metaClass.getStaticMetaMethod("createPositionsToNames", String.class)
        def positionsToNames = method.invoke(QueryWithNamedParametersTest.class, query)
        assertThat positionsToNames, equalTo(["another"])
    }

    @Test
    void in_the_middle_escaped_and_not_is_correctly_converted_into_plain_old() {
        def query = 'something \\$named_parameter $another something else'
        def method = PreparedStatementNamedParameters.metaClass.getStaticMetaMethod("convertToSimpleQuery", String.class)
        def simpleQuery = method.invoke(QueryWithNamedParametersTest.class, query)
        assertThat simpleQuery, equalTo('something $named_parameter ? something else')
    }

    @Test
    void twice_in_the_middle_is_correctly_mapped_to_position() {
        def query = 'something $named_parameter something $named_parameter else'
        def method = PreparedStatementNamedParameters.metaClass.getStaticMetaMethod("createPositionsToNames", String.class)
        def positionsToNames = method.invoke(QueryWithNamedParametersTest.class, query)
        assertThat positionsToNames, equalTo(["named_parameter", "named_parameter"])
    }

    @Test
    void twice_in_the_middle_is_correctly_converted_into_plain_old() {
        def query = 'something $named_parameter something $named_parameter else'
        def method = PreparedStatementNamedParameters.metaClass.getStaticMetaMethod("convertToSimpleQuery", String.class)
        def simpleQuery = method.invoke(QueryWithNamedParametersTest.class, query)
        assertThat simpleQuery, equalTo("something ? something ? else")
    }

    @Test
    void at_the_beginning_is_correctly_mapped_to_position() {
        def query = '$named_parameter something else'
        def method = PreparedStatementNamedParameters.metaClass.getStaticMetaMethod("createPositionsToNames", String.class)
        def positionsToNames = method.invoke(QueryWithNamedParametersTest.class, query)
        assertThat positionsToNames, equalTo(["named_parameter"])
    }

    @Test
    void at_the_beginning_is_correctly_converted_into_plain_old() {
        def query = '$named_parameter something else'
        def method = PreparedStatementNamedParameters.metaClass.getStaticMetaMethod("convertToSimpleQuery", String.class)
        def simpleQuery = method.invoke(QueryWithNamedParametersTest.class, query)
        assertThat simpleQuery, equalTo("? something else")
    }

    @Test
    void at_the_end_is_correctly_mapped_to_position() {
        def query = 'something else $named_parameter'
        def method = PreparedStatementNamedParameters.metaClass.getStaticMetaMethod("createPositionsToNames", String.class)
        def positionsToNames = method.invoke(QueryWithNamedParametersTest.class, query)
        assertThat positionsToNames, equalTo(["named_parameter"])
    }

    @Test
    void at_the_end_is_correctly_converted_into_plain_old() {
        def query = 'something else $named_parameter'
        def method = PreparedStatementNamedParameters.metaClass.getStaticMetaMethod("convertToSimpleQuery", String.class)
        def simpleQuery = method.invoke(QueryWithNamedParametersTest.class, query)
        assertThat simpleQuery, equalTo("something else ?")
    }

    @Test
    void test_bug_with_real_cassandra_query() {
        def query = 'UPDATE configuration_feedback USING TTL 10 SET message = $message, entity = $entity WHERE configuration = $configuration AND timestamp = $timestamp'
        def method = PreparedStatementNamedParameters.metaClass.getStaticMetaMethod("convertToSimpleQuery", String.class)
        def simpleQuery = method.invoke(QueryWithNamedParametersTest.class, query)
        assertThat simpleQuery, equalTo("UPDATE configuration_feedback USING TTL 10 SET message = ?, entity = ? WHERE configuration = ? AND timestamp = ?")
    }
}
