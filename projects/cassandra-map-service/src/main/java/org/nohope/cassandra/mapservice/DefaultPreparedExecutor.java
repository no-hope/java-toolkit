package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;

/**
 * @since 2014-09-25 18:02
 */
public class DefaultPreparedExecutor implements PreparedExecutor {
    private final Session session;
    private final BoundStatement bound;

    public DefaultPreparedExecutor(final Session session, final BoundStatement bound) {
        this.session = session;
        this.bound = bound;
    }

    @Override
    public void execute() {
        session.execute(bound);
    }

    @Override
    public ResultSetFuture executeAsync() {
        return session.executeAsync(bound);
    }

    @Override
    public BoundStatement getBound() {
        return bound;
    }
}
