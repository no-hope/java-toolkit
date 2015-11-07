package org.nohope.cassandra.mapservice;

import org.nohope.cassandra.mapservice.cfilter.CFilter;
import org.nohope.cassandra.mapservice.cfilter.CFilters;
import org.nohope.cassandra.mapservice.columns.CColumn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * External builder for {@link org.nohope.cassandra.mapservice.cfilter.CFilter filters}.
 * </p>
 * <p/>
 * <b>Example:</b>
 * <p/>
 * <p/>
 * We need to get from table persons with name exactly "John Smith", who already visits
 * us several time and has one or more kids.
 * <pre>
 * List<{@link org.nohope.cassandra.mapservice.cfilter.CFilter CFilter}> builderFilters =
 *     CFilterBuilder.getQueryFilters() // Filters map for get query
 *                   .eq("Name", "John Smith")  //value from column "Name" equals to "John Smith"
 *                   .lt("MorningVisitTime", DateTime.now()) //value from column "MorningVisitTime" lesser than current timestamp
 *                   .lte("VisitCount", 2) //value from column name "VisitCount" lesser or equal than 2
 *                   .gte("Children", 1) //value from column name "Children" greater or equal than 1
 *                   .getFilters();
 * </pre>
 * List of filters is used by {@link org.nohope.cassandra.mapservice.CQueryBuilder query builder} or
 * directly by {@link org.nohope.cassandra.mapservice.CQuery query}.
 */
public final class CFilterBuilder {
    private CFilterBuilder() {
    }

    /**
     * Gets subsets of available filters for get/remove.
     */
    public static GetFilters getQueryFilters() {
        return new GetFilters();
    }

    public static class GetFilters {
        private final List<CFilter<?>> filters = new ArrayList<>();

        GetFilters(final Collection<CFilter<?>> newFilters) {
            filters.addAll(newFilters);
        }

        public GetFilters() {
        }

        public <V> GetFilters eq(@Nonnull final CColumn<V, ?> column, @Nonnull final V value) {
            return addFilter(CFilters.eq(Value.bound(column, value)));
        }

        public <V> GetFilters gte(@Nonnull final CColumn<V, ?> column,
                                  @Nonnull final V value) {
            return addFilter(CFilters.gte(Value.bound(column, value)));
        }

        public <V> GetFilters gt(@Nonnull final CColumn<V, ?> column,
                                 @Nonnull final V value) {
            return addFilter(CFilters.gt(Value.bound(column, value)));
        }

        @SafeVarargs
        public final <V> GetFilters in(@Nonnull final CColumn<V, ?> column,
                                       @Nonnull final V... values) {
            return addFilter(CFilters.in(Value.bound(column.asList(), Arrays.asList(values))));
        }

        public <V> GetFilters lt(@Nonnull final CColumn<V, ?> column,
                                 @Nonnull final V value) {
            return addFilter(CFilters.lt(Value.bound(column, value)));
        }

        public <V> GetFilters lte(@Nonnull final CColumn<V, ?> column,
                                  @Nonnull final V value) {
            return addFilter(CFilters.lte(Value.bound(column, value)));
        }

        public List<CFilter<?>> getFilters() {
            return Collections.unmodifiableList(filters);
        }

        private GetFilters addFilter(final CFilter<?> newFilter) {
            final List<CFilter<?>> newFilters = new ArrayList<>();
            newFilters.addAll(filters);
            newFilters.add(newFilter);
            return new GetFilters(newFilters);
        }
    }
}
