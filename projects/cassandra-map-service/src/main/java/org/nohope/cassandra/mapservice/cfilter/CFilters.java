package org.nohope.cassandra.mapservice.cfilter;

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
     * @return {@link org.nohope.cassandra.mapservice.cfilter.EqFilter equals filter}
     */
    public static CFilter eq(final String key, final Object value) {
        return new EqFilter(key, value);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#in(String, Object...)}
     *
     * @param key   column name
     * @param value values
     * @return {@link org.nohope.cassandra.mapservice.cfilter.InFilter in filter}
     */
    public static CFilter in(final String key, final Object... value) {
        return new InFilter(key, value);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#lte(String, Object)}
     *
     * @param key   column name
     * @param value value
     * @return {@link org.nohope.cassandra.mapservice.cfilter.LesserThanOrEqualFilter lesser than or equals filter}
     */
    public static CFilter lte(final String key, final Object value) {
        return new LesserThanOrEqualFilter(key, value);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#lt(String, Object)}
     *
     * @param key   column name
     * @param value value
     * @return {@link org.nohope.cassandra.mapservice.cfilter.LesserThanFilter lesser than filter}
     */
    public static CFilter lt(final String key, final Object value) {
        return new LesserThanFilter(key, value);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#gte(String, Object)}
     *
     * @param key   column name
     * @param value value
     * @return {@link org.nohope.cassandra.mapservice.cfilter.GreaterThanOrEqualFilter greater or equal filter}
     */
    public static CFilter gte(final String key, final Object value) {
        return new GreaterThanOrEqualFilter(key, value);
    }

    /**
     * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#gt(String, Object)}
     *
     * @param key   column name
     * @param value value
     * @return {@link org.nohope.cassandra.mapservice.cfilter.GreaterThanFilter greater than filter}
     */
    public static CFilter gt(final String key, final Object value) {
        return new GreaterThanFilter(key, value);
    }
}
