package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.BoundStatement;

/**
 */
interface StatementExecutorProvider<T> {
    T getExecutor(BoundStatement bound);
}
