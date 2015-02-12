package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.nohope.cassandra.mapservice.cfilter.CFilter;
import org.nohope.cassandra.mapservice.columns.CColumn;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 */
public abstract class AbstractStatement<T> {
    private final CQuery cQuery;
    private final PreparedStatement preparedStatement;
    private final StatementExecutorProvider<T> statementExecutorProvider;
    private final TableScheme scheme;

    protected AbstractStatement(final CQuery cQuery,
                                final TableScheme scheme,
                                final PreparedStatement preparedStatement,
                                final StatementExecutorProvider<T> statementExecutorProvider) {
        this.scheme = scheme;
        this.cQuery = cQuery;
        this.preparedStatement = preparedStatement;
        this.statementExecutorProvider = statementExecutorProvider;
    }

    /**
     * Gets object from results' row.
     *
     * @param scheme     {@link org.nohope.cassandra.mapservice.TableScheme table scheme}
     * @param column     column to get result
     * @param row        {@link com.datastax.driver.core.Row datastax driver row}
     * @return the object from result
     */
    protected static Value<?> getObjectFromResult(final TableScheme scheme,
                                                  final CColumn<?, ?> column,
                                                  final Row row) {
        final CColumn erasure = (CColumn) column;
        return Value.bound(erasure, scheme.getColumns()
                                          .get(column.getName())
                                          .getConverter()
                                          .readValue(row, erasure));
    }

    protected Map<String, Value<?>> copyKeysFormFilters() {
        final Map<String, Value<?>> orderedFiltersMap = new LinkedHashMap<>();
        for (final CFilter<?> filter : cQuery.getFilters()) {
            final CColumn<?, ?> column = filter.getColumn();
            orderedFiltersMap.put(column.getName(), Value.unbound(column));
        }
        return orderedFiltersMap;
    }

    public class PreparedBinder {
        private final Map<String, Value<?>> bindKeysMap = copyKeysFormFilters();

        public <V> PreparedBinder bindTo(@Nonnull final CColumn<V, ?> key, @Nonnull final V object) {
            if (!bindKeysMap.containsKey(key.getName())) {
                throw new CQueryException(String.format("No such key as %s. Has keys %s.", key, bindKeysMap.keySet()));
            }
            bindKeysMap.put(key.getName(), Value.bound(key, object));
            return this;
        }

        public T stopBinding() {
            final Set<String> notBound = boundKeys();
            if (!notBound.isEmpty()) {
                throw new CQueryException(String.format(
                        "Keys %s wasn't bound for prepared statement. Keys were bounded %s",
                        notBound, getBoundedKeys(notBound)));
            }

            final BoundStatement bound = new BoundStatement(preparedStatement);
            final ColumnDefinitions meta = preparedStatement.getVariables();
            for (final Map.Entry<String, Value<?>> e : bindKeysMap.entrySet()) {
                final String key = e.getKey();
                if (!bound.isSet(key)) {
                    BindUtils.bind(bound, scheme, meta, key, e.getValue());
                }
            }

            return statementExecutorProvider.getExecutor(bound);
        }

        private Sets.SetView<String> getBoundedKeys(final Set<String> notBound) {
            return Sets.difference(bindKeysMap.keySet(), notBound);
        }

        private Set<String> boundKeys() {
            return Maps.filterValues(bindKeysMap, input -> input.getType() == Value.Type.UNBOUND).keySet();
        }
    }
}
