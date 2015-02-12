package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.util.RowNotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Prepared put query.<br>
 * <p/>
 * <p/>
 * Example of usage:
 * <pre>
 * {@link org.nohope.cassandra.mapservice.CQuery CQuery} query = {@link org.nohope.cassandra.mapservice.CQueryBuilder CQueryBuilder}.createQuery()
 *      .of(COL_QUOTES, COL_TIMESTAMP)
 *      .addFilters()
 *      .eq(COL_QUOTES)
 *      .lte(COL_TIMESTAMP)
 *      .noMoreFilters().end()
 *
 * CPreparedGet preparedQuery = {@link org.nohope.cassandra.mapservice.CMapService mapService}.prepareGet(RING_OF_POWER_TABLE, query);
 *
 * PreparedGetExecutor preparedExecutor = preparedQuery.bind()
 *      .bindTo(COL_QUOTES, quote)
 *      .bindTo(COL_TIMESTAMP, DateTime.now().toDate())
 *      .stopBinding();
 *
 *      preparedExecutor.execute()
 * </pre>
 * <p/>
 * Can be used with {@link org.nohope.cassandra.mapservice.CBatch batch operations}
 */
public final class CPreparedGet extends AbstractStatement<CPreparedGet.PreparedGetExecutor> {

    CPreparedGet(final PreparedStatement statement,
                 final CassandraFactory factory,
                 final CQuery cQuery,
                 final TableScheme scheme) {
        super(cQuery, scheme, statement, bound -> new PreparedGetExecutor(factory, bound, cQuery, scheme));
    }

    /**
     * Bind new values to pattern.
     *
     * @return the prepared binder
     */
    public PreparedBinder bind() {
        return new PreparedBinder();
    }

    /**
     * The type Prepared get executor.
     */
    public static class PreparedGetExecutor {
        private final BoundStatement bound;
        private final CassandraFactory factory;
        private final CQuery cQuery;

        private final Function<Row, ValueTuple> converter;

        public PreparedGetExecutor(final CassandraFactory factory,
                                   final BoundStatement bound,
                                   final CQuery cQuery,
                                   final TableScheme scheme) {
            this.bound = bound;
            this.factory = factory;
            this.cQuery = cQuery;

            this.converter = input -> {
                final Map<String, Value<?>> keyColumns = new HashMap<>();
                for (final CColumn<?, ?> column : cQuery.getExpectedColumnsCollection()) {
                    keyColumns.put(column.getName(), getObjectFromResult(scheme, column, input));
                }

                return new ValueTuple(keyColumns);
            };
        }

        /**
         * Get one row. <br>
         * Similar to {@link org.nohope.cassandra.mapservice.CMapSync#getOne(CQuery, com.datastax.driver.core.ConsistencyLevel)}
         *
         * @return {@link org.nohope.cassandra.mapservice.ValueTuple valueTuple}
         * @throws RowNotFoundException the row not found exception
         */
        public ValueTuple one() throws RowNotFoundException {
            final ResultSet result = factory.getSession().execute(bound);
            final Row row = result.one();
            if (row == null) {
                throw new RowNotFoundException(cQuery.getExpectedColumnsCollection().toString());
            }
            return converter.apply(row);
        }

        /**
         * Get all rows. <br>
         * Similar to {@link CMapSync#all()}
         *
         * @return the iterable of {@link org.nohope.cassandra.mapservice.ValueTuple valueTuples}
         */
        public Iterable<ValueTuple> all() {
            final ResultSet result = factory.getSession().execute(bound);
            return new OnceTraversableCIterable<>(Iterables.transform(result, converter));
        }
    }
}
