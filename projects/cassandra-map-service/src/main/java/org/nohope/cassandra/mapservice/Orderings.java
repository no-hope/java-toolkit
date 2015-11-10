package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.querybuilder.Ordering;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.nohope.cassandra.mapservice.columns.CColumn;

import java.util.function.Function;

/**
 * Wrapper over original datastax ordering.
 */
public enum Orderings {
    ASC(QueryBuilder::asc),
    DESC(QueryBuilder::desc);

    private final Function<String, Ordering> function;

    Orderings(final Function<String, Ordering> function) {
        this.function = function;
    }

    public Ordering forColumn(final CColumn<?, ?> col) {
        return function.apply(col.getName());
    }
}
