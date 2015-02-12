package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.mapservice.cfilter.CFilter;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.cops.Operation;
import org.nohope.cassandra.mapservice.ctypes.Converter;
import org.nohope.cassandra.mapservice.update.CUpdate;
import org.nohope.cassandra.util.RowNotFoundException;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Cassandra map sync implementation.
 * Map structure defines by {@link org.nohope.cassandra.mapservice.TableScheme scheme}
 * To define table structure use {@link org.nohope.cassandra.mapservice.CMapBuilder scheme builder}
 * To create instance of CMap use {@link org.nohope.cassandra.mapservice.CMapService CMapService}
 * <p/>
 * <b>Example:</b>
 * <p/>
 * CMapSync map = cMapServiceInstance.getMap("SomeSmartTableName")
 */
public final class CMapSync {
    private final TableScheme scheme;
    private final CassandraFactory cassandraFactory;
    private final CMapStatementGenerator mapStatement;

    /**
     * Instantiates a new CMap.
     *
     * @param scheme           Map description as a {@link org.nohope.cassandra.mapservice.TableScheme scheme}
     * @param cassandraFactory the cassandra factory
     */
    CMapSync(final TableScheme scheme, final CassandraFactory cassandraFactory) {
        this.scheme = scheme;
        this.cassandraFactory = cassandraFactory;
        this.mapStatement = new CMapStatementGenerator(scheme);
        init();
    }

    public void init() {
        cassandraFactory.getSession().execute(scheme.getTableDescription());
    }

    /**
     * Gets one.
     *
     * @param query {@link org.nohope.cassandra.mapservice.CQuery query}
     * @return {@link org.nohope.cassandra.mapservice.ValueTuple value tuple}
     * @throws RowNotFoundException the row not found exception
     * @throws CQueryException      query doesn't pass checks for some reason
     */
    public ValueTuple getOne(final CQuery query) throws RowNotFoundException {
        return getOne(query, null);
    }

    /**
     * Gets one with {@link com.datastax.driver.core.ConsistencyLevel consistency }.
     *
     * @param cQuery      {@link org.nohope.cassandra.mapservice.CQuery query}
     * @param consistency {@link com.datastax.driver.core.ConsistencyLevel consistency }.
     * @return {@link org.nohope.cassandra.mapservice.ValueTuple value tuple}
     * @throws RowNotFoundException the row not found exception
     * @throws CQueryException      query doesn't pass checks for some reason
     */
    public ValueTuple getOne(final CQuery cQuery, final ConsistencyLevel consistency)
            throws RowNotFoundException {

        final Row row = getRowFromResult(cQuery, mapStatement.get(cQuery, consistency));
        final Map<String, Value<?>> map = new HashMap<>();
        for (final CColumn<?, ?> column : cQuery.getExpectedColumnsCollection()) {
            map.put(column.getName(), getObjectFromResult(column, row));
        }
        return new ValueTuple(map);
    }

    /**
     * Get several results as iterable of value tuples.
     *
     * @param query {@link org.nohope.cassandra.mapservice.CQuery query}
     * @return the iterable of {@link org.nohope.cassandra.mapservice.ValueTuple value tuples}
     * @throws CMapServiceException the c map service exception
     * @throws CQueryException      query doesn't pass checks for some reason
     */
    public Iterable<ValueTuple> get(CQuery query) {
        return get(query, null);
    }

    /**
     * Get iterable.
     *
     * @param cQuery      the c query
     * @param consistency the consistency
     * @return the iterable
     * @throws CMapServiceException the c map service exception
     * @throws CQueryException      query doesn't pass checks for some reason
     */
    public Iterable<ValueTuple> get(CQuery cQuery, ConsistencyLevel consistency) {
        final ResultSet result = cassandraFactory.getSession().execute(mapStatement.get(cQuery, consistency));
        final ColumnsSet columnsToFetch = cQuery.getExpectedColumnsCollection();
        return new OnceTraversableCIterable<>(Iterables.transform(result, new Function<Row, ValueTuple>() {
            @Nullable
            @Override
            public ValueTuple apply(@Nullable Row input) {
                assert input != null;

                final Map<String, Value<?>> keyColumns = new HashMap<>();
                for (final CColumn<?, ?> column : columnsToFetch) {
                    keyColumns.put(column.getName(), getObjectFromResult(column, input));
                }
                return new ValueTuple(keyColumns);
            }
        })
        );
    }

    /**
     * Get all possible values map contains
     *
     * @return the iterable of {@link org.nohope.cassandra.mapservice.ValueTuple value tuples}
     */
    public Iterable<ValueTuple> all() {
        final Session session = cassandraFactory.getSession();
        final ResultSet result = session.execute(mapStatement.all());

        return new OnceTraversableCIterable<>(Iterables.transform(
                result, new Function<Row, ValueTuple>() {
                    @Nullable
                    @Override
                    public ValueTuple apply(@Nullable final Row input) {
                        assert input != null;
                        Map<String, Value<?>> keyColumns = new HashMap<>();
                        for (final CColumn<?, ?> column : scheme.getColumnsSet()) {
                            keyColumns.put(column.getName(), getObjectFromResult(column, input));
                        }
                        return new ValueTuple(keyColumns);
                    }
                })
        );
    }

    public long count(final CQuery cQuery) {
        final Session session = cassandraFactory.getSession();
        return session.execute(mapStatement.count(cQuery)).one().getLong(0);
    }

    /**
     * Remove some columns due to {@link org.nohope.cassandra.mapservice.CQuery query}
     *
     * @param cQuery {@link org.nohope.cassandra.mapservice.CQuery query}
     */
    public void remove(final CQuery cQuery) {
        remove(cQuery, null);
    }

    /**
     * Remove some columns due to {@link org.nohope.cassandra.mapservice.CQuery query}
     * with {@link com.datastax.driver.core.ConsistencyLevel consistency }.
     *
     * @param cQuery      {@link org.nohope.cassandra.mapservice.CQuery query}
     * @param consistency {@link com.datastax.driver.core.ConsistencyLevel consistency }.
     */
    public void remove(final CQuery cQuery, final ConsistencyLevel consistency) {
        cassandraFactory.getSession().execute(mapStatement.remove(cQuery, consistency));
    }

    /**
     * Put operation.
     *
     * @param query {@link org.nohope.cassandra.mapservice.CPutQuery}
     */
    public void put(final CPutQuery query) {
        put(query, null);
    }

    /**
     * Put with consistency.
     *
     * @param query            {@link org.nohope.cassandra.mapservice.CPutQuery}
     * @param consistencyLevel {@link com.datastax.driver.core.RegularStatement cassandra-driver regular statement}
     */
    public void put(final CPutQuery query, final ConsistencyLevel consistencyLevel) {
        final Session session = cassandraFactory.getSession();
        session.execute(mapStatement.put(query, consistencyLevel));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final CMapSync cMapSync = (CMapSync) o;
        return scheme.equals(cMapSync.scheme);
    }

    @Override
    public int hashCode() {
        return scheme.hashCode();
    }

    /**
     * <b>Package-local method for Batch operations</b>
     *
     * @param cQuery           {@link org.nohope.cassandra.mapservice.CQuery query}
     * @param consistencyLevel {@link com.datastax.driver.core.ConsistencyLevel cassandra-driver consistency level}
     * @return {@link com.datastax.driver.core.RegularStatement cassandra-driver regular statement}
     */
    RegularStatement createRemoveOperation(final CQuery cQuery, final ConsistencyLevel consistencyLevel) {
        return mapStatement.createRemoveOperation(cQuery, consistencyLevel);
    }

    /**
     * Create an update operation.
     *
     * @param update           {@link org.nohope.cassandra.mapservice.update.CUpdate}
     * @param consistencyLevel {@link com.datastax.driver.core.ConsistencyLevel cassandra-driver consistency level}
     * @return {@link com.datastax.driver.core.RegularStatement cassandra-driver regular statement}
     */
    RegularStatement createUpdateOperation(final CUpdate update, final ConsistencyLevel consistencyLevel) {
        final Update query = QueryBuilder.update(scheme.getTableNameQuoted());
        final Update.Where where = query.where();

        final Map<String, CColumn<?, ?>> columns = scheme.getColumns();
        for (final CFilter filter : update.getFilters()) {
            final CColumn<?, ?> column = columns.get(filter.getColumn().getName());
            final Converter converter = column.getConverter();
            where.and(filter.apply(converter));
        }

        for (final Operation<?> operation : update.getOperations()) {
            final CColumn<?, ?> column = columns.get(operation.getColumnName());
            final Converter converter = column.getConverter();
            where.with(operation.apply(converter));
        }

        if (null != consistencyLevel) {
            query.setConsistencyLevel(consistencyLevel);
        }
        return query;
    }

    /**
     * Package-local method for Batch operations
     *
     * @param cPutQuery        {@link org.nohope.cassandra.mapservice.CPutQuery}
     * @param consistencyLevel {@link com.datastax.driver.core.ConsistencyLevel cassandra-driver consistency level}
     * @return {@link com.datastax.driver.core.RegularStatement cassandra regular statement}
     */
    RegularStatement createPutOperation(final CPutQuery cPutQuery, final ConsistencyLevel consistencyLevel) {
        return mapStatement.createPutOperation(cPutQuery, consistencyLevel);
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
        return mapStatement.createDeleteQuery(cQuery, consistencyLevel);
    }

    private Value<?> getObjectFromResult(final CColumn<?, ?> columnName, final Row row) {
        final CColumn erasure = (CColumn) columnName;
        return Value.bound(erasure,
                scheme.getColumns()
                      .get(columnName.getName())
                      .getConverter()
                      .readValue(row, erasure));
    }

    private Row getRowFromResult(final CQuery cQuery, final Select.Where query) throws RowNotFoundException {
        final ResultSet result = cassandraFactory.getSession().execute(query);
        final Row row = result.one();
        if (row == null) {
            throw new RowNotFoundException(cQuery.getExpectedColumnsCollection().toString());
        }
        return row;
    }
}
