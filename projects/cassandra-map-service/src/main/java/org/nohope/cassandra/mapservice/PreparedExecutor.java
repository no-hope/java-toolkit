package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ResultSetFuture;

/**
 */
public interface PreparedExecutor {
    void execute();
    ResultSetFuture executeAsync();
    BoundStatement getBound();
}

