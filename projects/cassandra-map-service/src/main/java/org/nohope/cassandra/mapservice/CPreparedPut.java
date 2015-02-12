package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.*;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.mapservice.columns.CColumn;

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
    private static final String NOT_BOUND_MESSAGE =
            "Not all keys were bounded for prepared statement.\n Bounded {0}.\n Missed {1}.";

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

    private Map<String, Value<?>> copyKeysFromSchemeColumnsMap() {
        return Maps.transformEntries(
                scheme.getColumns(),
                new Maps.EntryTransformer<String, CColumn<?, ?>, Value<?>>() {
                    @Override
                    public Value<?> transformEntry(final String key, final CColumn<?, ?> value) {
                        return Value.unbound(value);
                    }
                }
        );
    }

    public class PreparedPutBinder {
        private final Map<String, Value<?>> bindKeysMap = new HashMap<>(copyKeysFromSchemeColumnsMap());
        private ConsistencyLevel consistencyLevel;

        public <T> PreparedPutBinder bindTo(final CColumn<T, ?> key, final T object) {
            if (!scheme.containsColumn(key)) {
                throw new CQueryException(
                        MessageFormat.format("No such column as {0}. Columns: {1}",
                                key,
                                scheme.getColumnsSet())
                );
            }
            bindKeysMap.put(key.getName(), Value.bound(key, object));
            return this;
        }

        public PreparedPutBinder setConsistencyLevel(final ConsistencyLevel consistencyLevel) {
            this.consistencyLevel = consistencyLevel;
            return this;
        }

        public PreparedPutExecutor stopBinding() {
            if (bindingsDontContainAllColumns()) {
                throw new CQueryException(
                        MessageFormat.format(NOT_BOUND_MESSAGE,
                                Sets.intersection(bindKeysMap.keySet(), scheme.getColumnNames()),
                                Sets.difference(bindKeysMap.keySet(), scheme.getColumnNames()))
                );
            }

            final BoundStatement bound = new BoundStatement(statement);
            if (consistencyLevel != null) {
                bound.setConsistencyLevel(consistencyLevel);
            }

            final ColumnDefinitions meta = statement.getVariables();
            for (final Map.Entry<String, Value<?>> e : bindKeysMap.entrySet()) {
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
                        BindUtils.bind(bound, scheme, meta, e.getValue());
                    }
                } catch (final RuntimeException exc) {
                    throw new IllegalStateException("Unexpected exception while processing field " + key, exc);
                }
            }
            return new PreparedPutExecutor(factory.getSession(), bound);
        }

        private boolean bindingsDontContainAllColumns() {
            return !bindKeysMap.keySet().containsAll(scheme.getColumnNames());
        }
    }

    // FIXME: remove that class!
    public static class PreparedPutExecutor extends DefaultPreparedExecutor {
        public PreparedPutExecutor(final Session session, final BoundStatement bound) {
            super(session, bound);
        }
    }
}
