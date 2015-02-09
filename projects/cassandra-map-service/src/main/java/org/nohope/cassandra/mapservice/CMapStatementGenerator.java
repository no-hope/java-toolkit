package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.*;
import com.google.common.collect.Sets;
import org.nohope.cassandra.mapservice.cfilter.CFilter;
import org.nohope.cassandra.mapservice.columns.CCollection;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static com.datastax.driver.core.querybuilder.QueryBuilder.timestamp;
import static com.datastax.driver.core.querybuilder.QueryBuilder.ttl;

/**
 */
final class CMapStatementGenerator {
    private static final String EXPECTED_COLUMNS_ERROR_MESSAGE = "Expected {0} columns {1}, but {2} were passed: {3}";
    private static final String UNEXPECTED_COLUMNS_ERROR_MESSAGE = "Unexpected columns passed {0}. Expected columns: {1}";

    private final TableScheme scheme;

    CMapStatementGenerator(final TableScheme scheme) {
        this.scheme = scheme;
    }

    private static void addConsistencyLevelToQuery(final ConsistencyLevel consistency,
                                                   final Select query) {
        if (null != consistency) {
            query.setConsistencyLevel(consistency);
        }
        query.disableTracing();
    }

    private static boolean noOrderingsInQuery(final CQuery cQuery) {
        return cQuery.getOrderBy().isEmpty();
    }

    public Select.Where get(final CQuery cQuery, final ConsistencyLevel consistency) {
        CFilterCheckTransporter.verifyQueryDueToScheme(scheme, cQuery);
        return prepareGetQuery(cQuery, consistency);
    }

    public Select all() {
        Select.Selection query = QueryBuilder.select();
        return query.from(scheme.getTableNameQuoted());
    }

    public RegularStatement remove(final CQuery cQuery, final ConsistencyLevel consistency) {
        return createDeleteQuery(cQuery, consistency);
    }

    public void put(final CPutQuery cPutQuery) {
        put(cPutQuery, null);
    }

    public Insert put(final CPutQuery cPutQuery, final ConsistencyLevel consistencyLevel) {
        return createPutStatement(cPutQuery, consistencyLevel);
    }

    public Statement count(final CQuery cQuery) {
        final Select.Builder query = QueryBuilder.select().countAll();
        return addWhereStatementToQuery(cQuery, query.from(scheme.getTableNameQuoted()));
    }

    /**
     * <b>Package-local method for Batch operations</b>
     *
     * @param cQuery           {@link org.nohope.cassandra.mapservice.CQuery query}
     * @param consistencyLevel {@link com.datastax.driver.core.ConsistencyLevel cassandra-driver consistency level}
     * @return {@link com.datastax.driver.core.RegularStatement cassandra-driver regular statement}
     */
    RegularStatement createRemoveOperation(final CQuery cQuery, final ConsistencyLevel consistencyLevel) {
        return createDeleteQuery(cQuery, consistencyLevel);
    }

    /**
     * Package-local method for Batch operations
     *
     * @param cPutQuery        the value tuple
     * @param consistencyLevel {@link com.datastax.driver.core.ConsistencyLevel cassandra-driver consistency level}
     * @return {@link com.datastax.driver.core.RegularStatement cassandra regular statement}
     */
    RegularStatement createPutOperation(final CPutQuery cPutQuery, final ConsistencyLevel consistencyLevel) {
        return createPutStatement(cPutQuery, consistencyLevel);
    }

    /**
     * Create delete query.
     * For inner delete query and batch operations.
     *
     * @param cQuery           {@link org.nohope.cassandra.mapservice.CQuery query}
     * @param consistencyLevel {@link com.datastax.driver.core.ConsistencyLevel consistency }.
     * @return the regular statement  {@link com.datastax.driver.core.RegularStatement cassandra regulate statement }.
     */
    RegularStatement createDeleteQuery(final CQuery cQuery, final ConsistencyLevel consistencyLevel) {
        final Delete.Where query = applyFiltersToDeleteQuery(cQuery);
        if (null != consistencyLevel) {
            query.setConsistencyLevel(consistencyLevel);
        }
        return query;
    }

    private Select.Where addWhereStatementToQuery(final CQuery cQuery, final Select query) {
        final Select.Where where = query.where();
        for (final CFilter filter : cQuery.getFilters()) {
            where.and(filter.apply(scheme.getColumns().get(filter.getColumnName()).getConverter()));
        }
        return where;
    }

    private Select addExpectedColumnsToQuery(final CQuery cQuery) {
        final Select.Selection query = QueryBuilder.select();
        for (final String column : cQuery.getExpectedColumnsCollection()) {
            query.column(column);
        }
        return query.from(scheme.getTableNameQuoted());
    }

    private Select.Where prepareGetQuery(final CQuery cQuery, final ConsistencyLevel consistency) {
        final Select query = addExpectedColumnsToQuery(cQuery);
        addConsistencyLevelToQuery(consistency, query);

        if (cQuery.getLimit().isPresent()) {
            query.limit(cQuery.getLimit().get());
        }

        if (cQuery.isAllowFiltering()) {
            addAllowFilteringAndOrderingsToQuery(cQuery, query);
        }

        return addWhereStatementToQuery(cQuery, query);
    }

    private void addAllowFilteringAndOrderingsToQuery(final CQuery cQuery, final Select query) {
        if (noOrderingsInQuery(cQuery)) {
            query.allowFiltering();
        } else {
            addWhereStatementToQuery(cQuery, query)
                    .orderBy(cQuery.getOrderingAsCassandraOrderings()).allowFiltering();
        }
    }

    private Delete.Where applyFiltersToDeleteQuery(final CQuery cQuery) {
        final Delete.Where query = QueryBuilder.delete().from(scheme.getTableNameQuoted()).where();
        for (final CFilter filter : getPrimaryKeysSet(cQuery)) {
            query.and(filter.apply(scheme.getColumns().get(filter.getColumnName()).getConverter()));
        }

        return query;
    }

    private Iterable<CFilter> getPrimaryKeysSet(final CQuery cQuery) {
        final Collection<CFilter> primaries = new ArrayList<>();

        for (final CFilter filter : cQuery.getFilters()) {
            final String columnName = filter.getColumnName();
            if (scheme.isPartitionKey(columnName) || scheme.isClusteringKey(columnName)) {
                primaries.add(filter);
            }
        }

        return primaries;
    }

    private void verifyColumns(final Set<String> columns) {
        if (columns.size() != scheme.getColumnsSet().size()) {
            throw new CQueryException(MessageFormat.format(
                    EXPECTED_COLUMNS_ERROR_MESSAGE,
                    scheme.getColumnsSet().size(),
                    columns.size(),
                    columns)
            );
        }
        if (!scheme.getColumnsSet().containsAll(columns)) {
            throw new CQueryException(MessageFormat.format(
                    UNEXPECTED_COLUMNS_ERROR_MESSAGE,
                    Sets.difference(columns, scheme.getColumnsSet()),
                    scheme.getColumnsSet()
            )
            );
        }
    }

    private Insert createPutStatement(final CPutQuery cPutQuery, final ConsistencyLevel consistencyLevel) {
        verifyColumns(cPutQuery.getValueTuple().get().getColumns().keySet());

        final Insert query = QueryBuilder.insertInto(scheme.getTableNameQuoted());
        final ValueTuple valueToPut = cPutQuery.getValueTuple().get();

        final Sets.SetView<String> collectionColumnSet = Sets.difference(valueToPut.getColumns().keySet(), scheme.getNonCollectionColumns());

        putNonCollectionValuesToQuery(query, valueToPut, collectionColumnSet);

        if (cPutQuery.getTTL().isPresent()) {
            query.using(ttl(cPutQuery.getTTL().get()));
        }
        if (cPutQuery.getTimestamp().isPresent()) {
            query.using(timestamp(cPutQuery.getTimestamp().get()));
        }
        if (null != consistencyLevel) {
            query.setConsistencyLevel(consistencyLevel);
        }

        if (!collectionColumnSet.isEmpty()) {
            final String queryAsAString = query.toString(); //ToString return query with values instead of ? markers
            final int indexOfFirstCloseBrace = queryAsAString.indexOf(')');
            final StringBuilder newQuery = new StringBuilder(queryAsAString.substring(0, indexOfFirstCloseBrace));
            for (final String column : collectionColumnSet) {
                if (valueToPut.getColumns().get(column) instanceof Iterable) {
                    newQuery.append(',');
                    newQuery.append(column);
                } else {
                    throw new CQueryException("Value for column " + column + " is not iterable: " + valueToPut.getColumns().get(column));
                }
            }
            newQuery.append(queryAsAString.substring(indexOfFirstCloseBrace));
            newQuery.replace(newQuery.length() - 2, newQuery.length(), ",");
            for (final String column : collectionColumnSet) {
                final CCollection collection = (CCollection) scheme.getColumns().get(column);
                newQuery.append(collection.insertToCollection((Iterable<?>) valueToPut.getColumns().get(column)));
                newQuery.append(',');
            }
            newQuery.replace(newQuery.length() - 1, newQuery.length(), ");");
            final Class<?> clazz = BuiltStatement.class;
            try {
                final Field cache = clazz.getDeclaredField("cache");
                cache.setAccessible(true);
                cache.set(query, newQuery.toString());
                final Field dirty = clazz.getDeclaredField("dirty");
                dirty.setAccessible(true);
                dirty.set(query, false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // FIXME: LOGGING
            }
        }
        return query;
    }

    private void putNonCollectionValuesToQuery(final Insert query,
                                               final ValueTuple valueToPut,
                                               final Collection<String> collectionColumn) {
        for (final String column : valueToPut.getColumns().keySet()) {
            if (!collectionColumn.contains(column)) {
                final Object cassandraValue = toCassandraType(valueToPut, column);
                query.getQueryString();
                query.value(column, cassandraValue);
            }
        }
    }

    private Object toCassandraType(final ValueTuple valueToPut, final String column) {
        return scheme.getColumns().get(column).getConverter().toCassandra(valueToPut.getColumns().get(column));
    }
}
