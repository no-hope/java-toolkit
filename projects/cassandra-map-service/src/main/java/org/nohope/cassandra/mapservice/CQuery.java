package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.querybuilder.Ordering;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.base.Optional;
import org.nohope.cassandra.mapservice.cfilter.CFilter;
import org.nohope.cassandra.mapservice.columns.CColumn;

import javax.annotation.concurrent.Immutable;
import java.util.*;

/**
 * Immutable query object.
 * <br>
 * Creating by {@link org.nohope.cassandra.mapservice.CQueryBuilder builder} or directly.
 * <p/>
 * <br> Using in {@link org.nohope.cassandra.mapservice.CMapSync#get(CQuery, com.datastax.driver.core.ConsistencyLevel) get }
 * <p/>
 * and {@link org.nohope.cassandra.mapservice.CMapSync#remove(CQuery) remove}
 * <p/>
 * {@link org.nohope.cassandra.mapservice.CMapSync#put(org.nohope.cassandra.mapservice.CPutQuery)}  put}
 * <p/>
 * uses {@link org.nohope.cassandra.mapservice.CPutQuery special put query}
 */
@Immutable
public final class CQuery {
    private final ColumnsSet columnsToGet;
    private final List<CFilter<?>> filters = new LinkedList<>();
    private final boolean allowFiltering;
    private final List<COrdering> orderings = new ArrayList<>();
    private final Optional<Integer> limit;

    private final boolean isPrepared;

    CQuery(final CColumn<?, ?>... expectedColumns) {
        this.isPrepared = false;
        this.columnsToGet = new ColumnsSet(expectedColumns);
        this.allowFiltering = false;
        this.limit = Optional.absent();
    }

    CQuery(final ColumnsSet expectedColumns,
           final Collection<CFilter<?>> filters,
           final boolean allowFiltering,
           final Collection<COrdering> orderBy,
           final boolean isPrepared,
           final Optional<Integer> limit) {
        this.limit = limit;
        this.isPrepared = isPrepared;
        this.columnsToGet = new ColumnsSet(expectedColumns);
        this.filters.addAll(filters);
        this.allowFiltering = allowFiltering;
        this.orderings.addAll(orderBy);
    }

    CQuery(final ColumnsSet expectedColumns,
           final boolean isPrepared) {
        this.isPrepared = isPrepared;
        columnsToGet = new ColumnsSet().withAll(expectedColumns);
        this.allowFiltering = false;
        this.limit = Optional.absent();
    }

    private static Ordering getProperCassandraOrdering(COrdering ordering) {
        if (ordering.isDesc()) {
            return QueryBuilder.desc(ordering.getColumn().getName());
        }
        return QueryBuilder.asc(ordering.getColumn().getName());
    }

    public Optional<Integer> getLimit() {
        return limit;
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    public ColumnsSet getExpectedColumnsCollection() {
        return new ColumnsSet().withAll(columnsToGet);
    }

    public List<CFilter<?>> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final CQuery query = (CQuery) o;
        return (allowFiltering == query.allowFiltering)
               && (isPrepared == query.isPrepared)
               && columnsToGet.equals(query.columnsToGet)
               && filters.equals(query.filters)
               && orderings.equals(query.orderings)
               && limit.equals(query.limit);
    }

    @Override
    public int hashCode() {
        int result = columnsToGet.hashCode();
        result = (31 * result) + filters.hashCode();
        result = (31 * result) + (allowFiltering ? 1 : 0);
        result = (31 * result) + orderings.hashCode();
        result = (31 * result) + (isPrepared ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CQuery{\n" +
               "columns: " + columnsToGet +
               "\nfilters: " + filters +
               "\nallowFiltering: " + allowFiltering +
               "\norderings: " + orderings +
               "\nisPrepared: " + isPrepared +
               "\nlimit: " + limit.get() +
               "\n}";
    }

    boolean isAllowFiltering() {
        return allowFiltering;
    }

    Collection<COrdering> getOrderBy() {
        return Collections.unmodifiableCollection(orderings);
    }

    Ordering[] getOrderingAsCassandraOrderings() {
        final Ordering[] arrayToReturn = new Ordering[orderings.size()];
        final List<Ordering> cassandraOrderings = new ArrayList<>();
        for (final COrdering ordering : orderings) {
            cassandraOrderings.add(getProperCassandraOrdering(ordering));
        }
        return cassandraOrderings.toArray(arrayToReturn);
    }

    List<COrdering> getCOrderingCollection() {
        return Collections.unmodifiableList(orderings);
    }
}
