package org.nohope.cassandra.factory;

import com.datastax.driver.core.*;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;

import javax.annotation.Nullable;

/**
 */
public final class QueryTracing {
    private QueryTracing() {
    }

    public static void log(final Logger logger, final Statement statement, final ResultSet resultSet) {
        final QueryTrace trace = resultSet.getExecutionInfo().getQueryTrace();
        if (trace != null) {
            final String events = Joiner.on("\n\t").join(
                    Iterables.transform(
                            trace.getEvents(), new Function<QueryTrace.Event, String>() {
                                @Nullable
                                @Override
                                public String apply(@Nullable final QueryTrace.Event input) {
                                    assert input != null;
                                    return input.getSourceElapsedMicros() + "us : " + input;
                                }
                            }));
            logger.info("Query {} trace:\n\t{}", statementToString(statement), events);
        }
    }

    public static String statementToString(final Statement statement) {
        if (statement instanceof BoundStatement) {
            return ((BoundStatement) statement).preparedStatement().getQueryString();
        } else if (statement instanceof SimpleStatement) {
            return ((SimpleStatement) statement).getQueryString();
        } else if (statement instanceof BatchStatement) {
            return "Batch: \n\t" + Joiner.on("\n\t").join(
                    Iterables.transform(
                            ((BatchStatement) statement).getStatements(), new Function<Statement, Object>() {
                                @Override
                                public String apply(final Statement input) {
                                    assert input != null;
                                    return statementToString(input);
                                }
                            }
                    ));
        }
        return statement.toString();
    }
}
