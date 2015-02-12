package org.nohope.cassandra.mapservice.cfilter;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.nohope.cassandra.mapservice.Value;
import org.nohope.cassandra.mapservice.columns.CColumn;

/**
 * factory for CFilters
 */
public final class CFilters {
    private CFilters() {
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#eq(String, Object)}
     *
     * @param key   column name
     * @param value value
     * @return filter bound to {@link QueryBuilder#eq(String, Object)} eq} expression
     */
    public static <V> CFilter<V> eq(final Value<V> value) {
        return new Filter<>(value, QueryBuilder::eq);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#lt(String, Object)}
     *
     * @param value value
     * @return filter bound to {@link QueryBuilder#lt(String, Object)} eq} expression
     */
    public static <V> CFilter<V> lt(final Value<V> value) {
        return new Filter<>(value, QueryBuilder::lt);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#lte(String, Object)}
     *
     * @param value value
     * @return filter bound to {@link QueryBuilder#lte(String, Object)} eq} expression
     */
    public static <V> CFilter<V> lte(final Value<V> value) {
        return new Filter<>(value, QueryBuilder::lte);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#gt(String, Object)}
     *
     * @param value value
     * @return filter bound to {@link QueryBuilder#gt(String, Object)} eq} expression
     */
    public static <V> CFilter<V> gt(final Value<V> value) {
        return new Filter<>(value, QueryBuilder::gt);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#gte(String, Object)}
     *
     * @param value value
     * @return filter bound to {@link QueryBuilder#gte(String, Object)} eq} expression
     */
    public static <V> CFilter<V> gte(final Value<V> value) {
        return new Filter<>(value, QueryBuilder::gte);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#in(String, Object...)}
     *
     * @param value values
     * @return filter bound to {@link QueryBuilder#in(String, Object...)} in} expression
     */
    public static <V> CFilter<V[]> in(final Value<V[]> value) {
        return new Filter<>(value, QueryBuilder::in);
    }
}
