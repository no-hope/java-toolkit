package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.mapservice.columns.CColumn;

import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>Example:</b>
 * <pre>
 * CPreparedPut preparedQuery = {@link org.nohope.cassandra.mapservice.CMapService mapService}.preparePut(RING_OF_POWER_TABLE);
 * {@link PreparedPutExecutor PreparedPutExecutor} preparedExecutor = preparedQuery.bind()
 *      .bindTo(COL_QUOTES, newQuote())
 *      .bindTo(COL_TIMESTAMP, DateTime.now().toDate())
 *      .stopBinding()
 * preparedExecutor.execute()
 * </pre>
 */
public final class CPreparedPut {
    private static final String MISSED_BOUND_COLUMNS_ERROR_MESSAGE = "Not all keys were bounded for prepared statement.\n Bounded {0}.\n Missed {1}.";
    private final PreparedStatement statement;
    private final CassandraFactory factory;
    private final TableScheme scheme;

    CPreparedPut(final PreparedStatement statement,
                 final CassandraFactory factory,
                 final TableScheme scheme) {
        this.statement = statement;
        this.factory = factory;
        this.scheme = scheme;
    }

    public PreparedPutBinder bind() {
        return new PreparedPutBinder();
    }

    private Map<String, Object> copyKeysFromSchemeColumnsMap() {
        return Maps.transformEntries(
                scheme.getColumns(),
                new Maps.EntryTransformer<String, Object, Object>() {
                    @Override
                    public Object transformEntry(
                            @Nullable String key,
                            @Nullable Object value) {
                        return QueryBuilder.bindMarker(key);
                    }
                }
        );
    }

    public class PreparedPutBinder {
        private final Map<String, Object> bindKeysMap = new HashMap<>(copyKeysFromSchemeColumnsMap());
        private ConsistencyLevel consistencyLevel;

        private PreparedPutBinder bindTo(final String key, final Object object) {
            if (!scheme.containsColumn(key)) {
                throw new CQueryException(
                        MessageFormat.format("No such column as {0}. Columns: {1}",
                                key,
                                scheme.getColumnsSet())
                );
            }
            bindKeysMap.put(key, object);
            return this;
        }

        public PreparedPutBinder setConsistencyLevel(final ConsistencyLevel consistencyLevel) {
            this.consistencyLevel = consistencyLevel;
            return this;
        }

        public PreparedPutExecutor stopBinding() {
            if (bindingsDontContainAllColumns()) {
                throw new CQueryException(
                        MessageFormat.format(MISSED_BOUND_COLUMNS_ERROR_MESSAGE,
                                Sets.intersection(bindKeysMap.keySet(), scheme.getColumnsSet()),
                                Sets.difference(bindKeysMap.keySet(), scheme.getColumnsSet()))
                );
            }
            final BoundStatement bound = new BoundStatement(statement);

            if (consistencyLevel != null) {
                bound.setConsistencyLevel(consistencyLevel);
            }

            final ColumnDefinitions meta = statement.getVariables();
            for (final Map.Entry<String, Object> e : bindKeysMap.entrySet()) {
                final String key = e.getKey();

                final Collection<ColumnDefinitions.Definition> filter = Collections2.filter(
                        bound.preparedStatement().getVariables().asList(),
                        new Predicate<ColumnDefinitions.Definition>() {
                            @Override
                            public boolean apply(final ColumnDefinitions.Definition input) {
                                return input.getName().equals(key);
                            }
                        });

                try {
                    if (!filter.isEmpty() && !bound.isSet(key)) {
                        final Object converted = scheme.getColumns().get(key).getConverter().toCassandra(e.getValue());
                        bound.setBytesUnsafe(key, meta.getType(key).serialize(converted, 1));
                    }
                } catch (final RuntimeException exc) {
                    throw new IllegalStateException("Unexpected exception while processing field " + key, exc);
                }
            }
            return new PreparedPutExecutor(factory.getSession(), bound);
        }

        private boolean bindingsDontContainAllColumns() {
            return !bindKeysMap.keySet().containsAll(scheme.getColumnsSet());
        }

        public <V, C> PreparedPutBinder bindTo(final CColumn<V, C> column, final V value) {
            return bindTo(column.getName(), value);
        }
    }

    // FIXME: remove that class!
    public static class PreparedPutExecutor extends DefaultPreparedExecutor {
        public PreparedPutExecutor(final Session session, final BoundStatement bound) {
            super(session, bound);
        }
    }
}
