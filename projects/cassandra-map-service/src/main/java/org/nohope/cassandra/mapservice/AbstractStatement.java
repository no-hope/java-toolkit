package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.BindMarker;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.base.Predicates;
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
     * @param columnName column name to get result
     * @param row        {@link com.datastax.driver.core.Row datastax driver row}
     * @return the object from result
     */
    protected static Object getObjectFromResult(final TableScheme scheme,
                                                final String columnName,
                                                final Row row) {
        return scheme.getColumns()
                     .get(columnName)
                     .getConverter()
                     .readValue(row, columnName);
    }

    protected Map<String, Object> copyKeysFormFilters() {
        final Map<String, Object> orderedFiltersMap = new LinkedHashMap<>();
        for (final CFilter filter : cQuery.getFilters()) {
            final String columnName = filter.getColumnName();
            orderedFiltersMap.put(columnName, QueryBuilder.bindMarker(columnName));
        }
        return orderedFiltersMap;
    }

    public class PreparedBinder {
        private final Map<String, Object> bindKeysMap = copyKeysFormFilters();

        private PreparedBinder bindTo(@Nonnull final String key, @Nonnull final Object object) {
            if (!bindKeysMap.containsKey(key)) {
                throw new CQueryException(String.format("No such key as %s. Has keys %s.", key, bindKeysMap.keySet()));
            }
            bindKeysMap.put(key, object);
            return this;
        }

        public <V> PreparedBinder bindTo(final CColumn<?, ?> column, final V value) {
            return bindTo(column.getName(), value);
        }

        public T stopBinding() {
            final Set<String> notBound = boundKeys();
            if (!notBound.isEmpty()) {
                throw new CQueryException(String.format(
                        "Keys %s were bound for prepared statement. Keys were bounded %s",
                        notBound,
                        getBoundedKeys(notBound)));
            }

            final BoundStatement bound = new BoundStatement(preparedStatement);
            final ColumnDefinitions meta = preparedStatement.getVariables();
            for (final Map.Entry<String, Object> e : bindKeysMap.entrySet()) {
                final String key = e.getKey();
                if (!bound.isSet(key)) {
                    final Object converted = scheme.getColumns().get(key).getConverter().toCassandra(e.getValue());
                    bound.setBytesUnsafe(key, meta.getType(key).serialize(converted, ProtocolVersion.V1));
                }
            }

            return statementExecutorProvider.getExecutor(bound);
        }

        private Sets.SetView<String> getBoundedKeys(final Set<String> notBound) {
            return Sets.difference(bindKeysMap.keySet(), notBound);
        }

        private Set<String> boundKeys() {
            return Maps.filterValues(bindKeysMap, Predicates.instanceOf(BindMarker.class)).keySet();
        }
    }
}
