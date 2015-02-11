package org.nohope.cassandra.mapservice.cfilter;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.nohope.cassandra.mapservice.columns.CColumn;

/**
 * factory for CFilters
 */
public final class CFilters {
    private CFilters() {
    }

    public static <V> CFilter<V> eq(final CColumn<V, ?> key, final V value) {
        return new Filter<>(key, value, QueryBuilder::eq);
    }

    public static <V> CFilter<V> lt(final CColumn<V, ?> key, final V value) {
        return new Filter<>(key, value, QueryBuilder::lt);
    }

    public static <V> CFilter<V> lte(final CColumn<V, ?> key, final V value) {
        return new Filter<>(key, value, QueryBuilder::lte);
    }

    public static <V> CFilter<V> gt(final CColumn<V, ?> key, final V value) {
        return new Filter<>(key, value, QueryBuilder::gt);
    }

    public static <V> CFilter<V> gte(final CColumn<V, ?> key, final V value) {
        return new Filter<>(key, value, QueryBuilder::gte);
    }

    @SafeVarargs
    public static <V> CFilter<V[]> in(final CColumn<V, ?> key, final V... value) {
        return new Filter<>(key, value, QueryBuilder::in);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#eq(String, Object)}
     *
     * @param key   column name
     * @param value value
     * @return {@link org.nohope.cassandra.mapservice.cfilter.EqFilter equals filter}
     */
    @Deprecated
    public static <V> CFilter<V> eq(final String key, final V value) {
        return new EqFilter<>(key, value);
    }


    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#in(String, Object...)}
     *
     * @param key   column name
     * @param value values
     * @return {@link org.nohope.cassandra.mapservice.cfilter.InFilter in filter}
     */
    @Deprecated
    public static <V> CFilter<V[]> in(final String key, final V... value) {
        return new InFilter<>(key, value);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#lte(String, Object)}
     *
     * @param key   column name
     * @param value value
     * @return {@link org.nohope.cassandra.mapservice.cfilter.LesserThanOrEqualFilter lesser than or equals filter}
     */
    @Deprecated
    public static <V> CFilter<V> lte(final String key, final V value) {
        return new LesserThanOrEqualFilter<>(key, value);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#lt(String, Object)}
     *
     * @param key   column name
     * @param value value
     * @return {@link org.nohope.cassandra.mapservice.cfilter.LesserThanFilter lesser than filter}
     */
    @Deprecated
    public static <V> CFilter<V> lt(final String key, final V value) {
        return new LesserThanFilter<>(key, value);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#gte(String, Object)}
     *
     * @param key   column name
     * @param value value
     * @return {@link org.nohope.cassandra.mapservice.cfilter.GreaterThanOrEqualFilter greater or equal filter}
     */
    @Deprecated
    public static <V> CFilter<V> gte(final String key, final V value) {
        return new GreaterThanOrEqualFilter<>(key, value);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#gt(String, Object)}
     *
     * @param key   column name
     * @param value value
     * @return {@link org.nohope.cassandra.mapservice.cfilter.GreaterThanFilter greater than filter}
     */
    @Deprecated
    public static <V> CFilter<V> gt(final String key, final V value) {
        return new GreaterThanFilter<>(key, value);
    }
}
