package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import org.nohope.cassandra.factory.CassandraFactory;

/**
 * <b>Example: </b>
 * <pre>
 * {@link org.nohope.cassandra.mapservice.CQuery CQuery} delQuery = CQueryBuilder
 *      .createRemoveQuery()
 *      .addFilters()
 *      .eq(COL_QUOTES)
 *      .noMoreFilters().end()
 * {@link org.nohope.cassandra.mapservice.CPreparedRemove} preparedDelQuery = {@link org.nohope.cassandra.mapservice.CMapService mapService}.prepareRemove(RING_OF_POWER_TABLE, delQuery);
 *
 * {@link PreparedRemoveExecutor PreparedRemoveExecutor} executor = preparedDelQuery.bind()
 *       .bindTo(COL_QUOTES, quote)
 *       .stopBinding()
 *
 * executor.execute()
 *
 * </pre>
 */
public final class CPreparedRemove extends AbstractStatement<CPreparedRemove.PreparedRemoveExecutor> {
    public CPreparedRemove(final PreparedStatement statement,
                           final CassandraFactory factory,
                           final CQuery cQuery,
                           final TableScheme scheme,
                           final ConsistencyLevel consistencyLevel) {
        super(cQuery, scheme, statement, new StatementExecutorProvider<PreparedRemoveExecutor>() {
            @Override
            public PreparedRemoveExecutor getExecutor(final BoundStatement boundStatement) {
                boundStatement.setConsistencyLevel(consistencyLevel);
                return new PreparedRemoveExecutor(factory.getSession(), boundStatement);
            }
        });
    }

    public PreparedBinder bind() {
        return new PreparedBinder();
    }

    // FIXME: remove that class!
    public static class PreparedRemoveExecutor extends DefaultPreparedExecutor {
        public PreparedRemoveExecutor(final Session session, final BoundStatement bound) {
            super(session, bound);
        }
    }
}
