package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.ConsistencyLevel;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.mapservice.columns.CColumn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage for {@link org.nohope.cassandra.mapservice.CMapSync maps} in some scope, batch operations provider (on
 * maps, created with this particular service instance)
 * <p/>
 * Preferably created using {@link org.nohope.cassandra.mapservice.CMapServiceFactory factory}
 * <p/>
 * On instantiating creates all maps due to given {@link org.nohope.cassandra.mapservice.TableScheme TableSchemes}
 * <p/>
 * <p/>
 * <ol>
 * <b>Examples:</b>
 * <p/>
 * List<TableScheme> schemes = Lists.newArrayList(someTableSchemeObject);
 * <p/>
 * <li>CMapService service = serviceFactory.getService(schemes);</li>
 * <p/>
 * <li>CMapService service = new CMapService(cassandraFactoryObject, schemes);</li>
 * </ol>
 */
public final class CMapService {
    private final CassandraFactory factory;
    private final Map<String, ImmutablePair<TableScheme, CMapSync>> cassandraMaps = new ConcurrentHashMap<>();

    CMapService(final CassandraFactory factory, final TableScheme... schemes) {
        this(factory, Arrays.asList(schemes));
    }

    CMapService(final CassandraFactory factory, final Iterable<TableScheme> schemes) {
        this.factory = factory;
        buildMaps(schemes);
    }

    private static Map<String, Value<?>> copyKeysFromSchemeColumnsMap(final TableScheme scheme) {
        return Maps.transformEntries(scheme.getColumns(),
                new Maps.EntryTransformer<String, CColumn<?, ?>, Value<?>>() {
                    @Override
                    public Value<?> transformEntry(@Nullable final String key, @Nullable CColumn<?, ?>
                            value) {
                        return Value.unbound(value);
                    }
                });
    }

    public CMapSync getMap(final String id) {
        if (cassandraMaps.containsKey(id)) {
            return cassandraMaps.get(id).getRight();
        }
        throw new CMapServiceException("No such map " + id);
    }

    /**
     * Provides batch operations for maps created in this service object
     *
     * @return batch operation builder
     */
    public CBatch batch() {
        return this.batch(null);
    }

    public CBatch batch(final ConsistencyLevel consistency) {
        return new CBatchImpl(this, factory, consistency);
    }

    /**
     * Provides prepared operations for maps created in this service object
     *
     * @return batch operation builder
     */
    public CPreparedGet prepareGet(@Nonnull final String id,
                                   @Nonnull final CQuery cQuery) {
        return prepareGet(id, cQuery, null);
    }

    public CPreparedGet prepareGet(@Nonnull final String id,
                                   @Nonnull final CQuery cQuery,
                                   @Nullable final ConsistencyLevel consistency) {
        if (!cQuery.isPrepared()) {
            throw new IllegalArgumentException("Not a prepared cQuery");
        }
        final TableScheme scheme = getScheme(id);
        final CMapStatementGenerator statementGenerator = new CMapStatementGenerator(scheme);
        final String stringQuery = statementGenerator.get(cQuery, consistency).toString();
        return new CPreparedGet(factory.getSession().prepare(stringQuery), factory, cQuery, scheme);
    }

    public CPreparedRemove prepareRemove(@Nonnull final String id, @Nonnull final CQuery cQuery) {
        return prepareRemove(id, cQuery, null);
    }

    public CPreparedRemove prepareRemove(@Nonnull final String id,
                                         @Nonnull final CQuery cQuery,
                                         @Nullable final ConsistencyLevel consistency) {
        if (!cQuery.isPrepared()) {
            throw new IllegalArgumentException("Not a prepared cQuery");
        }
        final TableScheme scheme = getScheme(id);
        final CMapStatementGenerator statementGenerator = new CMapStatementGenerator(scheme);
        final String stringQuery = statementGenerator.remove(cQuery, consistency).toString();
        return new CPreparedRemove(factory.getSession().prepare(stringQuery), factory, cQuery, scheme, consistency);
    }

    public CPreparedPut preparePut(@Nonnull final String id,
                                   @Nonnull final CPutQuery query) {
        return preparePut(id, query, null);
    }

    public CPreparedPut preparePut(@Nonnull final String id,
                                   @Nonnull final CPutQuery query,
                                   @Nullable final ConsistencyLevel consistency) {
        final TableScheme scheme = getScheme(id);
        final CMapStatementGenerator statementGenerator = new CMapStatementGenerator(scheme);
        ValueTuple tuple = new ValueTuple(copyKeysFromSchemeColumnsMap(scheme));

        final Optional<ValueTuple> valueTuple = query.getValueTuple();
        if (valueTuple.isPresent()) {
            tuple = tuple.with(valueTuple.get());
        }

        final CPutQuery newQuery = new CPutQuery(Optional.of(tuple), query.getTTL());
        final String statement = statementGenerator.put(newQuery, consistency).toString();
        return new CPreparedPut(factory.getSession().prepare(statement), factory, scheme);
    }

    public CPreparedPut preparePut(@Nonnull final String id) {
        return preparePut(id, (ConsistencyLevel) null);
    }

    public CPreparedPut preparePut(@Nonnull final String id, @Nullable final ConsistencyLevel consistency) {
        final TableScheme scheme = getScheme(id);
        final CMapStatementGenerator statementGenerator = new CMapStatementGenerator(scheme);
        final CPutQuery query = new CPutQuery(Optional.of(new ValueTuple(copyKeysFromSchemeColumnsMap(scheme))));
        final String statement = statementGenerator.put(query, consistency).toString();
        return new CPreparedPut(factory.getSession().prepare(statement), factory, scheme);
    }

    private void buildMaps(final Iterable<TableScheme> schemes) {
        for (final TableScheme scheme : schemes) {
            final String tableName = scheme.getTableName();
            if (cassandraMaps.containsKey(tableName)) {
                throw new IllegalArgumentException("Table '" + tableName + "' is already defined!");
            }
            cassandraMaps.put(tableName, new ImmutablePair<>(scheme, new CMapSync(scheme, factory)));
        }
    }

    private TableScheme getScheme(final String id) {
        if (cassandraMaps.containsKey(id)) {
            return cassandraMaps.get(id).getLeft();
        }
        throw new CMapServiceException("No such map " + id);
    }

    public void extend(final Iterable<TableScheme> ret) {
        buildMaps(ret);
    }
}

