package org.nohope.cassandra.mapservice;

import org.nohope.cassandra.mapservice.cfilter.CFilter;
import org.nohope.cassandra.mapservice.cfilter.CFilters;

import javax.annotation.Nonnull;
import java.util.ArrayList;
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
        private final List<CFilter> filters = new ArrayList<>();

        GetFilters(final Collection<CFilter> newFilters) {
            filters.addAll(newFilters);
        }

        public GetFilters() {
        }

        public GetFilters eq(@Nonnull final String columnName,
                             final Object value) {
            return addFilter(CFilters.eq(columnName, value));
        }

        public GetFilters gte(@Nonnull final String columnName,
                              @Nonnull final Object value) {
            return addFilter(CFilters.gte(columnName, value));
        }

        public GetFilters gt(@Nonnull final String columnName,
                             @Nonnull final Object value) {
            return addFilter(CFilters.gt(columnName, value));
        }

        public GetFilters in(@Nonnull final String columnName,
                             @Nonnull final Object... values) {
            return addFilter(CFilters.in(columnName, values));
        }

        public GetFilters lt(@Nonnull final String columnName,
                             @Nonnull final Object value) {
            return addFilter(CFilters.lt(columnName, value));
        }

        public GetFilters lte(@Nonnull final String columnName,
                              @Nonnull final Object value) {
            return addFilter(CFilters.lte(columnName, value));
        }

        public List<CFilter> getFilters() {
            return Collections.unmodifiableList(filters);
        }

        private GetFilters addFilter(final CFilter newFilter) {
            final List<CFilter> newFilters = new ArrayList<>();
            newFilters.addAll(filters);
            newFilters.add(newFilter);
            return new GetFilters(newFilters);
        }
    }
}
