package org.nohope.cassandra.mapservice.cfilter;

import com.datastax.driver.core.querybuilder.QueryBuilder;
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
    public static <V> CFilter<V> eq(final CColumn<V, ?> key, final V value) {
        return new Filter<>(key, value, QueryBuilder::eq);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#lt(String, Object)}
     *
     * @param key   column name
     * @param value value
     * @return filter bound to {@link QueryBuilder#lt(String, Object)} eq} expression
     */
    public static <V> CFilter<V> lt(final CColumn<V, ?> key, final V value) {
        return new Filter<>(key, value, QueryBuilder::lt);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#lte(String, Object)}
     *
     * @param key   column name
     * @param value value
     * @return filter bound to {@link QueryBuilder#lte(String, Object)} eq} expression
     */
    public static <V> CFilter<V> lte(final CColumn<V, ?> key, final V value) {
        return new Filter<>(key, value, QueryBuilder::lte);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#gt(String, Object)}
     *
     * @param key   column name
     * @param value value
     * @return filter bound to {@link QueryBuilder#gt(String, Object)} eq} expression
     */
    public static <V> CFilter<V> gt(final CColumn<V, ?> key, final V value) {
        return new Filter<>(key, value, QueryBuilder::gt);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#gte(String, Object)}
     *
     * @param key   column name
     * @param value value
     * @return filter bound to {@link QueryBuilder#gte(String, Object)} eq} expression
     */
    public static <V> CFilter<V> gte(final CColumn<V, ?> key, final V value) {
        return new Filter<>(key, value, QueryBuilder::gte);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#in(String, Object...)}
     *
     * @param key   column name
     * @param value values
     * @return filter bound to {@link QueryBuilder#in(String, Object...)} in} expression
     */
    @SafeVarargs
    public static <V> CFilter<V[]> in(final CColumn<V, ?> key, final V... value) {
        return new Filter<>(key, value, QueryBuilder::in);
    }
}
