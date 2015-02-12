package org.nohope.cassandra.mapservice;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.nohope.cassandra.mapservice.columns.CColumn;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a tuple of row values
 * <b>Example:</b>
 * <pre>
 * {@link org.nohope.cassandra.mapservice.CMapSync CMapSync} map = service.getMap("mapToPut") // service as a {@link org.nohope.cassandra.mapservice.CMapService CMapService}
 * </pre>
 * <p/>
 * <ol>
 * <li>For using with put wrap ValueTuple with {@link org.nohope.cassandra.mapservice.CPutQuery special put query}
 * <pre>
 * {@link org.nohope.cassandra.mapservice.ValueTuple ValueTuple} valueToPut =
 *     ValueTuple.of(COL_QUOTE, newQuote())
 *               .with(COL_TIMESTAMP, new Date())
 *               .with(COL_QUOTE_UUID, UUID.randomUUID())
 *
 * map.put(new CPutQuery(valueToPut))
 *     </pre>
 * </li>
 * <li>For get using
 * <pre>
 * {@link org.nohope.cassandra.mapservice.CQuery CQuery} query =
 *     {@link org.nohope.cassandra.mapservice.CQueryBuilder CQueryBuilder}.createQuery()
 *                  .of(COL_QUOTE, COL_TIMESTAMP, COL_QUOTE_UUID)
 *                  .end();
 *
 * {@link org.nohope.cassandra.mapservice.ValueTuple ValueTuple} valueToGet = map.getOne(query);
 *     </pre>
 * </li>
 * </ol>
 */
@Immutable
public final class ValueTuple {
    private final Map<String, Value<?>> columns = new HashMap<>();

    ValueTuple(final Map<String, Value<?>> mapToPut) {
        columns.putAll(mapToPut);
    }

    public static <T> ValueTuple of(@Nonnull final CColumn<T, ?> column,
                                    @Nonnull final T value) {
        // FIXME: check if it function call or bind marker

        return new ValueTuple(Collections.singletonMap(column.getName(), Value.bound(column, value)));
    }

    public static <T> ValueTuple of(@Nonnull final Value<T> value) {
        return new ValueTuple(Collections.singletonMap(value.getColumn().getName(), value));
    }

    public <T> ValueTuple with(@Nonnull final CColumn<T, ?> column, @Nonnull final T value) {
        final Map<String, Value<?>> newMap = new HashMap<>(columns);
        newMap.put(column.getName(), Value.bound(column, value));
        return new ValueTuple(newMap);
    }

    public <T> ValueTuple with(@Nonnull final Value<T> value) {
        final Map<String, Value<?>> newMap = new HashMap<>(columns);
        newMap.put(value.getColumn().getName(), value);
        return new ValueTuple(newMap);
    }

    public ValueTuple with(@Nonnull final ValueTuple tuple) {
        final Map<String, Value<?>> newMap = new HashMap<>(columns);
        newMap.putAll(tuple.columns);
        return new ValueTuple(newMap);
    }

    /**
     * Typed get if you know what to get.
     *
     * @param <V>  the type parameter
     * @param column the column
     * @return the t
     */
    @SuppressWarnings("unchecked")
    public <V> V get(final CColumn<V, ?> column) {
        final String name = column.getName();
        if (columns.containsKey(name)) {
            return (V) columns.get(name).getValue();
        }
        throw new IllegalArgumentException(MessageFormat.format(
                "No such column as {0}. Has columns: {1}", name, columns));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final ValueTuple that = (ValueTuple) o;
        return columns.equals(that.columns);
    }

    @Override
    public String toString() {
        return "ValueTuple{" +
               "columns=" + columns +
               '}';
    }

    @Override
    public int hashCode() {
        return columns.hashCode();
    }

    Map<String, Value<?>> getValues() {
        return Collections.unmodifiableMap(columns);
    }

    Map<String, CColumn<?, ?>> getColumns() {
        return Collections.unmodifiableMap(Maps.transformValues(columns,
                new Function<Value<?>, CColumn<?, ?>>() {
                    @Override
                    public CColumn<?, ?> apply(final Value<?> input) {
                        return input.getColumn();
                    }
                }));
    }
}
