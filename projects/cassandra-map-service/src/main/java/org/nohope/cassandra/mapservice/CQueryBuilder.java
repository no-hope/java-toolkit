package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.base.Optional;
import org.joda.time.ReadableDuration;
import org.nohope.cassandra.mapservice.cfilter.CFilter;
import org.nohope.cassandra.mapservice.cfilter.CFilters;
import org.nohope.cassandra.mapservice.columns.CColumn;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>Builder for {@link CQuery queries}.</p>
 * <p/>
 * <b>Example:</b>
 * <pre>
 * ColumnsSet COLUMNS = new {@link ColumnsSet ColumnsSet}("column1", "column2");
 * </pre>
 * <p/>
 * <ol>
 * <li>Simple query with one filter
 * <pre>
 * CQuery query =
 *     CQueryBuilder.createQuery()
 *                  .of(COLUMNS)
 *                  .addFilters()
 *                  .lte("column1", 4)
 *                  .noMoreFilters()
 *                  .end();
 *     </pre>
 * </li>
 *
 * <li>Allow filtering
 * <pre>
 * CQuery query =
 *     CQueryBuilder.createQuery()
 *                  .of("column1", "column2")
 *                  .allowFiltering()
 *                  .end();
 *     </pre>
 * </li>
 *
 * <li>With ordering
 * <pre>
 * CQuery query =
 *     CQueryBuilder.createQuery()
 *                  .of(COLUMNS)
 *                  .allowFiltering()
 *                  .orderingBy("column1", {@link Orderings#ASC Orderings.ASC})
 *                  .end();
 *     </pre>
 * </li>
 * <li>With filters
 * <pre>
 * CQuery query =
 *     CQueryBuilder.createQuery()
 *                  .of(COLUMNS)
 *                  .withFilters({@link org.nohope.cassandra.mapservice.cfilter.CFilters CFilters}.{@link org.nohope.cassandra.mapservice.cfilter.CFilters#eq(String, Object) eq}("column1", 2),
 *                               {@link org.nohope.cassandra.mapservice.cfilter.CFilters CFilters}.{@link org.nohope.cassandra.mapservice.cfilter.CFilters#gt(String, Object) gt}("column2", 4))
 *                  .end();
 *     </pre>
 * </li>
 * <li>Using built-in filters builder
 * <pre>
 * CQuery query =
 *     CQueryBuilder.createQuery()
 *                  .of(COLUMNS)
 *                  .addFilters()
 *                      .eq("column2", "value")
 *                      .lte("column1", 4)
 *                  .noMoreFilters()
 *                  .end()
 *     </pre>
 * </li>
 * </ol>
 */
public final class CQueryBuilder {

    private CQueryBuilder() {
    }

    /**
     * Create query.
     *
     * @return the inner query builder
     */
    public static InnerQueryBuilder createQuery() {
        return new InnerQueryBuilder();
    }

    public static InnerPreparedQueryBuilder createPreparedQuery() {
        return new InnerPreparedQueryBuilder();
    }

    /**
     * Create CPutQuery for put and prepared put.
     * <p/>
     * <ol>
     * <li> Create simple put query
     * <pre>
     * CPutQuery query =
     *  CQueryBuilder.createPutQuery()
     *      .addValueTuple(ValueTuple.of("xxx", "yyy"))
     *      .end()
     *     </pre>
     * </li>
     * <li> Create put query with TTL in seconds
     * <pre>
     * CPutQuery query =
     *  CQueryBuilder.createPutQuery()
     *      .addValueTuple(ValueTuple.of("xxx", "yyy"))
     *      .withTTL(1000)
     *      </pre>
     * </li>
     *
     * <li> Create put query with TTL using joda.time.Duration
     * <pre>
     * CPutQuery query =
     *  CQueryBuilder.createPutQuery()
     *      .addValueTuple(ValueTuple.of("xxx", "yyy"))
     *      .withTTL(Duration.millis(111))
     *      </pre>
     * </li>
     * </ol>
     *
     * @return the put inner query builder
     */
    public static PutInnerQueryBuilder createPutQuery() {
        return new PutInnerQueryBuilder();
    }

    /**
     * Shortcut for creation remove query.
     * <p/>
     * <pre>
     * CQuery query =
     *  CQueryBuilder
     *       .createRemoveQuery()
     *       .withFilters(filters)
     *       .end()
     * </pre>
     *
     * @return the remove inner query builder
     */
    public static InnerQueryBuilder.CQueryFilters createRemoveQuery() {
        return new InnerQueryBuilder().of();
    }

    public static InnerPreparedQueryBuilder.CPreparedQueryFilters createPreparedRemoveQuery() {
        return new InnerPreparedQueryBuilder().empty();
    }

    /**
     * Shortcut for creating a count query.
     *
     * @return CQueryFilters filters builders
     */
    public static InnerQueryBuilder.CQueryFilters createCountQuery() {
        return new InnerQueryBuilder().of();
    }

    public static class PutInnerQueryBuilder {
        private Optional<ValueTuple> valueTuple;

        public PutQueryOptions addValueTuple(@Nonnull ValueTuple valueTuple) {
            this.valueTuple = Optional.of(new ValueTuple(valueTuple.getColumns()));
            return new PutQueryOptions();
        }

        public PutQueryOptions allValues() { // TODO: XXX: dirty
            this.valueTuple = Optional.absent();
            return new PutQueryOptions();
        }

        public class PutQueryOptions {
            public CPutQuery end() {
                return new CPutQuery(valueTuple);
            }

            public CPutQuery withTTL(final int ttlInSeconds) {
                return new CPutQuery(valueTuple, Optional.of(ttlInSeconds));
            }

            public CPutQuery withTTL(final ReadableDuration duration) {
                final long seconds = TimeUnit.MILLISECONDS.toSeconds(duration.getMillis());
                if (seconds > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException("Duration "
                                                       + duration
                                                       + " exceeds Integer.MAX_VALUE seconds");
                }

                //noinspection NumericCastThatLosesPrecision
                return withTTL((int) seconds);
            }
        }
    }

    public static class InnerPreparedQueryBuilder {

        private final List<CFilter> filters = new LinkedList<>();
        private final List<COrdering> orderings = new ArrayList<>();
        private ColumnsSet columnsToGet;
        private boolean isPrepared = true;
        private Optional<Integer> limit = Optional.absent();

        /**
         * Of expected columns
         *
         * @param expectedColumns the expected columns
         * @return the c query filters
         */
        public CPreparedQueryFilters of(final String... expectedColumns) {
            columnsToGet = new ColumnsSet(expectedColumns);
            return new CPreparedQueryFilters();
        }

        /**
         * Of expected columns
         *
         * @param columnSet the column set
         * @return the c query filters
         */
        public CPreparedQueryFilters of(final ColumnsSet columnSet) {
            columnsToGet = new ColumnsSet().withAll(columnSet);
            return new CPreparedQueryFilters();
        }

        public CPreparedQueryFilters of(final CColumn<?, ?>... cColumns) {
            return of(CMapBuilder.getNames(cColumns));
        }

        CPreparedQueryFilters empty() {
            columnsToGet = new ColumnsSet();
            return new CPreparedQueryFilters();
        }

        /**
         * Inner main filters builder
         */
        public class CPreparedQueryFilters {

            /**
             * Add filters manually.
             *
             * @return the get filters
             */
            public GetPreparedFilters addFilters() {
                return new GetPreparedFilters();
            }

            /**
             * Get current query.
             *
             * @return the c query
             */
            public CQuery end() {
                return new CQuery(columnsToGet, filters, false, orderings, isPrepared, limit);
            }

            /**
             * Allow filtering.
             *
             * @return the c query allow filtering
             */
            public CQueryAllowFiltering allowFiltering() {
                return new CQueryAllowFiltering();
            }
        }

        public class GetPreparedFilters {
            public GetPreparedFilters() {
                isPrepared = true;
            }

            /**
             * Eq get filters.
             *
             * @param columnName the column name
             * @return the get filters
             */
            public GetPreparedFilters eq(@Nonnull final String columnName) {
                filters.add(CFilters.eq(columnName, QueryBuilder.bindMarker(columnName)));
                return this;
            }

            /**
             * Gte get filters.
             *
             * @param columnName the column name
             * @return the get filters
             */
            public GetPreparedFilters gte(@Nonnull final String columnName) {
                filters.add(CFilters.gte(columnName, QueryBuilder.bindMarker(columnName)));
                return this;
            }

            public GetPreparedFilters gte(@Nonnull final CColumn<?, ?> column) {
                return gte(column.getName());
            }

            /**
             * Gt get filters.
             *
             * @param columnName the column name
             * @return the get filters
             */
            public GetPreparedFilters gt(@Nonnull final String columnName) {
                filters.add(CFilters.gt(columnName, QueryBuilder.bindMarker(columnName)));
                return this;
            }

            public GetPreparedFilters gt(@Nonnull final CColumn<?, ?> column) {
                return gt(column.getName());
            }

            /**
             * Lt get filters.
             *
             * @param columnName the column name
             * @return the get filters
             */
            public GetPreparedFilters lt(@Nonnull final String columnName) {
                filters.add(CFilters.lt(columnName, QueryBuilder.bindMarker(columnName)));
                return this;
            }

            public GetPreparedFilters lt(@Nonnull final CColumn<?, ?> column) {
                return lt(column.getName());
            }

            /**
             * Lte get filters.
             *
             * @param columnName the column name
             * @return the get filters
             */
            public GetPreparedFilters lte(@Nonnull final String columnName) {
                filters.add(CFilters.lte(columnName, QueryBuilder.bindMarker(columnName)));
                return this;
            }

            public GetPreparedFilters lte(@Nonnull final CColumn<?, ?> column) {
                return lte(column.getName());
            }

            /**
             * No more filters.
             *
             * @return the c query end filters adding
             */
            public CQueryEndFiltersAdding noMoreFilters() {
                return new CQueryEndFiltersAdding();
            }

            public GetPreparedFilters eq(final CColumn<?, ?> column) {
                return eq(column.getName());
            }
        }

        /**
         * The type C query end filters adding.
         */
        public class CQueryEndFiltersAdding {

            /**
             * End c query.
             *
             * @return the c query
             */
            public CQuery noFiltering() {
                return new CQuery(columnsToGet, filters, false, orderings, isPrepared, limit);
            }

            public CQuery withLimit(final int limitValue) {
                limit = Optional.of(limitValue);
                return new CQuery(columnsToGet, filters, true, orderings, isPrepared, limit);
            }

            /**
             * Ordering by.
             *
             * @param columnName the column name
             * @param order      the order
             * @return the c query ordering by
             */
            public CQueryOrderingBy orderingBy(@Nonnull final String columnName, final Orderings order) {
                orderings.add(new COrdering(columnName, order));
                return new CQueryOrderingBy();
            }

            public CQueryAllowFiltering allowFiltering() {
                return new CQueryAllowFiltering();
            }
        }

        /**
         * The type C query allow filtering.
         */
        public class CQueryAllowFiltering {

            /**
             * End c query.
             *
             * @return the c query
             */
            public CQuery end() {
                return new CQuery(columnsToGet, filters, true, orderings, isPrepared, limit);
            }

            public CQuery withLimit(final int limitValue) {
                limit = Optional.of(limitValue);
                return new CQuery(columnsToGet, filters, true, orderings, isPrepared, limit);
            }
        }

        /**
         * The type C query ordering by.
         */
        public class CQueryOrderingBy {
            public CQuery withLimit(final int limitValue) {
                limit = Optional.of(limitValue);
                return new CQuery(columnsToGet, filters, true, orderings, isPrepared, limit);
            }

            /**
             * End c query.
             *
             * @return the c query
             */
            public CQuery end() {
                return new CQuery(columnsToGet, filters, false, orderings, isPrepared, limit);
            }

            /**
             * Allow filtering.
             *
             * @return the c query allow filtering
             */
            public CQueryAllowFiltering allowFiltering() {
                return new CQueryAllowFiltering();
            }
        }
    }

    /**
     * The main Inner builder for queries
     */
    public static class InnerQueryBuilder {

        private final List<CFilter> filters = new LinkedList<>();
        private final List<COrdering> orderings = new ArrayList<>();
        private ColumnsSet columnsToGet;
        private boolean isPrepared = true;
        private Optional<Integer> limit = Optional.absent();

        /**
         * Of expected columns
         *
         * @param columnSet the column set
         * @return the c query filters
         */
        public CQueryFilters of(final ColumnsSet columnSet) {
            columnsToGet = new ColumnsSet().withAll(columnSet);
            return new CQueryFilters();
        }

        public CQueryFilters of(final CColumn<?, ?>... columns) {
            columnsToGet = new ColumnsSet(CMapBuilder.getNames(columns));
            return new CQueryFilters();
        }

        /**
         * Inner main filters builder
         */
        public class CQueryFilters {

            /**
             * With filters as collection.
             *
             * @param filtersList the filters list
             * @return the c query end filters adding
             */
            public CQueryEndFiltersAdding withFilters(final Collection<CFilter> filtersList) {
                filters.addAll(filtersList);
                isPrepared = false;
                return new CQueryEndFiltersAdding();
            }

            /**
             * With filters as var arg.
             *
             * @param filters the filters list
             * @return the c query end filters adding
             */
            public CQueryEndFiltersAdding withFilters(final CFilter... filters) {
                isPrepared = false;
                return withFilters(Arrays.asList(filters));
            }

            /**
             * Add filters manually.
             *
             * @return the get filters
             */
            public GetFilters addFilters() {
                isPrepared = false;
                return new GetFilters();
            }

            /**
             * Get current query.
             *
             * @return the c query
             */
            public CQuery end() {
                return new CQuery(columnsToGet, filters, false, orderings, isPrepared, limit);
            }

            /**
             * Ordering by.
             *
             * @param column the column name
             * @param order  the order
             * @return the c query ordering by
             */
            public CQueryOrderingBy orderingBy(@Nonnull final CColumn<?, ?> column, final Orderings order) {
                orderings.add(new COrdering(column.getName(), order));
                return new CQueryOrderingBy();
            }

            /**
             * Allow filtering.
             *
             * @return the c query allow filtering
             */
            public CQueryAllowFiltering allowFiltering() {
                return new CQueryAllowFiltering();
            }
        }

        /**
         * The type Get filters.
         */
        public class GetFilters {

            /**
             * Eq get filters.
             *
             * @param column the column name
             * @param value  the value
             * @return the get filters
             */
            public GetFilters eq(@Nonnull final CColumn<?, ?> column, final Object value) {
                filters.add(CFilters.eq(column.getName(), value));
                return this;
            }

            /**
             * Gte get filters.
             *
             * @param column the column name
             * @param value  the value
             * @return the get filters
             */
            public GetFilters gte(@Nonnull final CColumn<?, ?> column, @Nonnull final Object value) {
                filters.add(CFilters.gte(column.getName(), value));
                return this;
            }

            /**
             * Gt get filters.
             *
             * @param column the column name
             * @param value  the value
             * @return the get filters
             */
            public GetFilters gt(@Nonnull final CColumn<?, ?> column, @Nonnull final Object value) {
                filters.add(CFilters.gt(column.getName(), value));
                return this;
            }

            /**
             * In get filters.
             *
             * @param column the column name
             * @param values the values
             * @return the get filters
             */
            public GetFilters in(@Nonnull final CColumn<?, ?> column, @Nonnull final Object... values) {
                filters.add(CFilters.in(column.getName(), values));
                return this;
            }

            /**
             * Lt get filters.
             *
             * @param column the column name
             * @param value  the value
             * @return the get filters
             */
            public GetFilters lt(@Nonnull final CColumn<?, ?> column, @Nonnull final Object value) {
                filters.add(CFilters.lt(column.getName(), value));
                return this;
            }

            /**
             * Lte get filters.
             *
             * @param column the column name
             * @param value  the value
             * @return the get filters
             */
            public GetFilters lte(@Nonnull final CColumn<?, ?> column, @Nonnull final Object value) {
                filters.add(CFilters.lte(column.getName(), value));
                return this;
            }

            /**
             * No more filters.
             *
             * @return the c query end filters adding
             */
            public CQueryEndFiltersAdding noMoreFilters() {
                return new CQueryEndFiltersAdding();
            }
        }

        /**
         * The type C query end filters adding.
         */
        public class CQueryEndFiltersAdding {

            /**
             * End c query.
             *
             * @return the c query
             */
            public CQuery end() {
                return new CQuery(columnsToGet, filters, false, orderings, isPrepared, limit);
            }

            /**
             * Ordering by.
             *
             * @param column the column name
             * @param order  the order
             * @return the c query ordering by
             */
            public CQueryOrderingBy orderingBy(@Nonnull final CColumn<?, ?> column, final Orderings order) {
                orderings.add(new COrdering(column.getName(), order));
                return new CQueryOrderingBy();
            }

            public CQueryAllowFiltering allowFiltering() {
                return new CQueryAllowFiltering();
            }
        }

        /**
         * The type C query allow filtering.
         */
        public class CQueryAllowFiltering {

            /**
             * End c query.
             *
             * @return the c query
             */
            public CQuery end() {
                return new CQuery(columnsToGet, filters, true, orderings, isPrepared, limit);
            }

            public CQuery withLimit(final int limitValue) {
                limit = Optional.of(limitValue);
                return new CQuery(columnsToGet, filters, true, orderings, isPrepared, limit);
            }
        }

        /**
         * The type C query ordering by.
         */
        public class CQueryOrderingBy {
            public CQuery withLimit(final int limitValue) {
                limit = Optional.of(limitValue);
                return new CQuery(columnsToGet, filters, true, orderings, isPrepared, limit);
            }

            /**
             * End c query.
             *
             * @return the c query
             */
            public CQuery end() {
                return new CQuery(columnsToGet, filters, false, orderings, isPrepared, limit);
            }

            /**
             * Allow filtering.
             *
             * @return the c query allow filtering
             */
            public CQueryAllowFiltering allowFiltering() {
                return new CQueryAllowFiltering();
            }
        }
    }
}
